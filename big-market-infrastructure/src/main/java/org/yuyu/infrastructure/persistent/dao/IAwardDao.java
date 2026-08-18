package org.yuyu.infrastructure.persistent.dao;


import org.apache.ibatis.annotations.Mapper;
import org.yuyu.infrastructure.persistent.po.Award;

import java.util.List;

/**
 * 奖品表Dao
 */
@Mapper
public interface IAwardDao {
    List<Award> queryAwardList();
}
