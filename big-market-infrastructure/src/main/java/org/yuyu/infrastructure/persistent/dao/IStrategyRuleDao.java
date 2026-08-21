package org.yuyu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.yuyu.infrastructure.persistent.po.StrategyRule;

import java.util.List;

/**
 * 策略规则表Dao
 */
@Mapper
public interface IStrategyRuleDao {
    List<StrategyRule> queryStrategyRuleList();

    StrategyRule queryStrategyRule(StrategyRule strategyRuleReq);

    String queryStrategyRuleValue(StrategyRule strategyRule);
}
