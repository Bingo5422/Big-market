package org.yuyu.infrastructure.persistent.repository;

import org.springframework.stereotype.Repository;
import org.yuyu.domain.strategy.model.entity.StrategyAwardEntity;
import org.yuyu.domain.strategy.model.entity.StrategyEntity;
import org.yuyu.domain.strategy.model.entity.StrategyRuleEntity;
import org.yuyu.domain.strategy.repository.IStrategyRepository;
import org.yuyu.infrastructure.persistent.dao.IStrategyAwardDao;
import org.yuyu.infrastructure.persistent.dao.IStrategyDao;
import org.yuyu.infrastructure.persistent.dao.IStrategyRuleDao;
import org.yuyu.infrastructure.persistent.po.Strategy;
import org.yuyu.infrastructure.persistent.po.StrategyAward;
import org.yuyu.infrastructure.persistent.po.StrategyRule;
import org.yuyu.infrastructure.persistent.redis.IRedisService;
import org.yuyu.types.common.Constants;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 策略仓储实现
 */
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IStrategyAwardDao strategyAwardDao;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;


    @Resource
    private IRedisService redisService;

    /**
     * 根据抽奖策略ID 查询 其对应的奖品实体列表
     */
    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {

        // 1 先从Redis中查询
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);

        if (strategyAwardEntities != null && !strategyAwardEntities.isEmpty()) {
            return strategyAwardEntities;
        }

        // 2 如果Redis中没有，则从数据库中查询
        List<StrategyAward> strategyAwards = strategyAwardDao.queryStrategyAwardListByStrategyId(strategyId);


        //3 把查询到的结果再存回Redis
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());

        //3.1 把从库中查询到的StrategyAward对象转换为StrategyAwardEntity对象
        for(StrategyAward strategyAward: strategyAwards){
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                        .strategyId(strategyAward.getStrategyId())
                        .awardId(strategyAward.getAwardId())
                        .awardCount(strategyAward.getAwardCount())
                        .awardCountSurplus(strategyAward.getAwardCountSurplus())
                        .awardRate(strategyAward.getAwardRate())
                        .build();

            strategyAwardEntities.add(strategyAwardEntity);
        }

        //3.2 存回 redis
        redisService.setValue(cacheKey, strategyAwardEntities);

        return strategyAwardEntities;
    }

    /**
     * 存储 策略奖品概率查找表 到 Redis
     */
    @Override
    public void storeStrategyAwardSearchRateTable(String key, int size, Map<Integer, Integer> shuffleStrategyAwardSearchRateTables) {

        // 1. 存储抽奖策略范围值【总槽位】，如10000，用于生成随机数
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, size);
        // 2. 存储概率查找表
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(shuffleStrategyAwardSearchRateTables);
    }

    @Override
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    @Override
    public int getRateRange(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }

    //从 Redis 缓存的“概率查找表”中，以O(1) 的时间复杂度快速查出本次抽奖中出的奖品 ID
    @Override
    public Integer getStrategyAwardAssemble(String key, int rateKey) {
        return (Integer) redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key).get(rateKey);
    }

    @Override
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        //优先从缓存中获取
        String cacheKey = Constants.RedisKey.STRATEGY_KEY+strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if(strategyEntity != null){
            return strategyEntity;
        }

        //缓存中不存在，从数据库中获取
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);

        //把从数据库中获取的Strategy类型转换为StrategyEntity类型
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        //存回缓存
        redisService.setValue(cacheKey, strategyEntity);

        return strategyEntity;

    }

    @Override
    public StrategyRuleEntity queryStrategyRuleEntity(Long strategyId, String ruleModel) {

        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRuleRes = strategyRuleDao.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleRes.getStrategyId())
                .awardId(strategyRuleRes.getAwardId())
                .ruleType(strategyRuleRes.getRuleType())
                .ruleModel(strategyRuleRes.getRuleModel())
                .ruleValue(strategyRuleRes.getRuleValue())
                .ruleDesc(strategyRuleRes.getRuleDesc())
                .build();
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
        return strategyRuleDao.queryStrategyRuleValue(strategyRule);
    }


}
