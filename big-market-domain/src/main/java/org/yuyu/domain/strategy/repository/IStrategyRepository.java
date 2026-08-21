package org.yuyu.domain.strategy.repository;
import org.yuyu.domain.strategy.model.entity.StrategyAwardEntity;
import org.yuyu.domain.strategy.model.entity.StrategyEntity;
import org.yuyu.domain.strategy.model.entity.StrategyRuleEntity;

import java.util.List;
import java.util.Map;

/**
 * 策略仓储接口
 */
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeStrategyAwardSearchRateTable(String key, int size, Map<Integer, Integer> shuffleStrategyAwardSearchRateTable);

    int getRateRange(Long strategyId);

    int getRateRange(String key);

    Integer getStrategyAwardAssemble(String key, int rateKey);

    StrategyEntity queryStrategyEntityByStrategyId(Long strategyId);

    StrategyRuleEntity queryStrategyRuleEntity(Long strategyId, String ruleModel);

    String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel);
}
