package org.yuyu.domain.strategy.service.armory;

/**
 * 策略抽奖的调度
 */
public interface IStrategyDispatch {
    /**
     * 获取抽奖策略装配的随机结果
     * @param strategyId
     * @return
     */
    Integer getRandomAwardId(Long strategyId);


    /**
     * 获取 根据rule_weight过滤后的 抽奖策略装配的随机结果
     * @param strategyId
     * @param ruleWeightValue
     * @return
     */
    Integer getRandomAwardId(Long strategyId,String ruleWeightValue);

}
