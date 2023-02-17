package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.service.IOrderInfoService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-17 017 20:50
 */
@RocketMQMessageListener(
        topic = MQConstant.ORDER_PAY_TIMEOUT_TOPIC,
        selectorExpression = MQConstant.ORDER_PENDING_TAG,
        consumerGroup = MQConstant.ORDER_PENDING_GROUP
)
@Slf4j
@Component
public class PendingOrderMessageListener implements RocketMQListener<OrderMessage> {

    @Autowired
    private IOrderInfoService orderInfoService;

    @Override
    public void onMessage(OrderMessage message) {
        log.info("[创建订单监听器] 收到新的订单，请查收：{}", JSON.toJSONString(message));
        orderInfoService.createOrder(message.getUserPhone(), message.getSeckillId());
    }
}
