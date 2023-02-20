package cn.wolfcode.mq.listener;

import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.service.IOrderInfoService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-18 018 11:23
 */
@RocketMQMessageListener(
        consumerGroup = "DELAY_ORDER_GROUP",
        topic = MQConstant.ORDER_PAY_TIMEOUT_TOPIC
)
@Component
@Slf4j
public class DelayOrderMessageListener implements RocketMQListener<OrderMQResult> {

    @Autowired
    private IOrderInfoService orderInfoService;


    @Override
    public void onMessage(OrderMQResult result) {
        log.info("[延迟订单监听器] 收到新的延迟订单消息，准备检查是否已支付：{}", JSON.toJSONString(result));
        orderInfoService.checkOrderTimeout(result.getOrderNo(), result.getSeckillId());
        log.info("[延迟订单监听器] 延迟订单检查完毕........");
    }
}
