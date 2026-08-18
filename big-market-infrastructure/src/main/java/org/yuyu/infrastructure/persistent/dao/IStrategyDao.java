package org.yuyu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.yuyu.infrastructure.persistent.po.Strategy;

import java.util.List;

/**
 * 抽奖策略表Dao
 */
@Mapper
public interface IStrategyDao {
    List<Strategy> queryStrategyList();
}
