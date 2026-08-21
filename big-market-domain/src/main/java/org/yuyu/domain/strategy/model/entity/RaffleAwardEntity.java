package org.yuyu.domain.strategy.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户最终抽奖抽到的奖品实体
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardEntity {
    //策略Id
    private Long strategyId;
    //奖品Id
    private Integer awardId;
    //奖品对接标识----------每一个都是一个对应的发奖策略
    private String awardKey;
    //奖品配置信息
    private String awardConfig;
    //奖品内容描述
    private String awardDesc;
}
