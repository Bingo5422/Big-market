package org.yuyu.domain.strategy.service.armory;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yuyu.domain.strategy.model.entity.StrategyAwardEntity;
import org.yuyu.domain.strategy.model.entity.StrategyEntity;
import org.yuyu.domain.strategy.model.entity.StrategyRuleEntity;
import org.yuyu.domain.strategy.repository.IStrategyRepository;
import org.yuyu.types.enums.ResponseCode;
import org.yuyu.types.exception.AppException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;


@Service
@Slf4j
public class StrategyArmoryDispatch implements IStrategyArmory,IStrategyDispatch{

    @Resource
    private IStrategyRepository repository;

    /**
     *
     * @param strategyId
     * @return
     */
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1 查询策略配置
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);

        //2 装配全量策略
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        //3 权重策略配置，适用于rule_weight

        //3.1 查询策略实体配置的抽奖规则模型是否包含 rule_weight
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        String ruleWeight = strategyEntity.getRuleWeight();
        //rule_models里没有rule_weight这一项直接返回
        if(ruleWeight == null){
            return true;
        }

        //3.2 查询： 根据 策略ID 和 rule_model中是rule_weight 的策略规则实体
        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRuleEntity(strategyId, ruleWeight);

        if(null == strategyRuleEntity){
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(),ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        Map<String, List<Integer>> ruleWeightValueMap = strategyRuleEntity.getRuleWeightValues();
        Set<String> keys = ruleWeightValueMap.keySet();
        for (String key : keys) {
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntitiesClone);
        }



        return true;
    }


    /**
     *
     * 策略装配函数
     * 核心作用：在活动开始前（或首次抽奖前），将某个策略下所有奖品的复杂概率计算，
     * 预先转化为一个乱序的“索引-奖品ID”映射表（Map），并持久化到 Redis 缓存中，为极速抽奖做准备。
     * @param key
     * @param strategyAwardEntities
     */

    private void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities) {
        // 1. 获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取概率值总和
        BigDecimal totalAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 用 1 % 0.0001 获得概率范围（总槽位数），百分位、千分位、万分位
        BigDecimal rateRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        // 4. 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());

        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();

            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }
        // 5. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateTables);

        // 6. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        //    key是概率的整数值，value是对应奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateTables.get(i));
        }

        // 7. 存放到 Redis
        repository.storeStrategyAwardSearchRateTable(key, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);

    }



    /**
     * 抽奖执行函数
     * 核心作用：在用户发起抽奖时，直接利用 Redis 中的装配数据，
     * 在 O(1) 时间复杂度 内完成高并发抽奖。
     * @param strategyId
     * @return
     */
    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }

    /**
     *
     * @param strategyId
     * @param ruleWeightValue
     * @return
     */
    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));
    }



}
