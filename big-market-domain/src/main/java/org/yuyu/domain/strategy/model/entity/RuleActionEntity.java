package org.yuyu.domain.strategy.model.entity;


import lombok.*;
import org.yuyu.domain.strategy.model.vo.RuleLogicCheckTypeVO;

/**
 * 规则动作实体-----会影响后面流程的变化
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleActionEntity <T extends RuleActionEntity.RaffleEntity>{

    private String code = RuleLogicCheckTypeVO.ALLOW.getCode();

    private String info = RuleLogicCheckTypeVO.ALLOW.getInfo();

    private String ruleModel;

    private T data;


    static public class RaffleEntity{

    }

    //抽奖前
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static public class RaffleBeforeEntity extends RaffleEntity{
        //策略ID
        private Long strategyId;
        //权重值key：用于抽奖时可以选择权重抽奖
        private String ruleWeightValueKey;
        //奖品ID
        private Integer awardId;
    }


    //抽奖中
    static public class RaffleDuringEntity extends RaffleEntity{


    }


    //抽奖后
    static public class RaffleAfterEntity extends RaffleEntity{


    }


}
