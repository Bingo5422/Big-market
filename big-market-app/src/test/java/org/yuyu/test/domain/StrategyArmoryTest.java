package org.yuyu.test.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yuyu.domain.strategy.service.armory.IStrategyArmory;
import org.yuyu.domain.strategy.service.armory.IStrategyDispatch;

import javax.annotation.Resource;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyArmoryTest {
    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IStrategyDispatch strategyDispatch;

    /**
     * 测试策略装配（预热）
     * 拟活动初始化/后台配置阶段，将 ID 为 100001L 的抽奖策略进行装配
     * 校验方法：运行此测试后，打开 Redis-Commander（http://localhost:8081），
     * 检查 Redis 中是否成功生成了对应的 RMap 以及槽位范围 key
     */
    @Before
    public void test_strategyArmory() {
        strategyArmory.assembleLotteryStrategy(100001L);
    }


    /**
     * 全量
     * 模拟 3 次真实的抽奖请求。
     * 预期控制台输出：会打印出 3 个根据概率随机获取到的奖品 ID
     */

    @Test
    public void test_getAssembleAwardID() {
        log.info("测试结果：{} - 奖品ID值", strategyDispatch.getRandomAwardId(100001L));
        log.info("测试结果：{} - 奖品ID值", strategyDispatch.getRandomAwardId(100001L));
        log.info("测试结果：{} - 奖品ID值", strategyDispatch.getRandomAwardId(100001L));
    }


    /**
     * 根据策略ID+权重值，从装配的策略中随机获取奖品ID值
     */
    @Test
    public void test_getRandomAwardId_ruleWeightValue() {
        log.info("测试结果：{} - 4000 策略配置", strategyDispatch.getRandomAwardId(100001L, "4000:102,103,104,105"));
        log.info("测试结果：{} - 5000 策略配置", strategyDispatch.getRandomAwardId(100001L, "5000:102,103,104,105,106,107"));
        log.info("测试结果：{} - 6000 策略配置", strategyDispatch.getRandomAwardId(100001L, "6000:102,103,104,105,106,107,108,109"));
    }

}
