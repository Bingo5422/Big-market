package org.yuyu.domain.strategy.service.armory;


/**
 * 策略装配工厂，负责初始化策略计算
 */


public interface IStrategyArmory {

    /**
     * 装配抽奖策略配置
     * 触发时机可以为活动审核通过后开始配置
     * @param strategyId
     * @return
     */
    boolean assembleLotteryStrategy(Long strategyId);


    }
