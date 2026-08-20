package org.yuyu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.yuyu.infrastructure.persistent.po.StrategyAward;

import java.util.List;

/**
 * 抽奖策略奖品明细配置表Dao
 */
@Mapper
public interface IStrategyAwardDao {

    /**
     * 根据抽奖策略ID查询其对应的奖品列表
     */
    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);


    List<StrategyAward> queryStrategyAwardList();


}
