package cn.wolfcode.mq.listener;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.mq.callback.DefaultSendCallback;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
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
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(OrderMessage msg) {
        log.info("[创建订单监听器] 收到新的订单，请查收：{}", JSON.toJSONString(msg));
        OrderMQResult result = new OrderMQResult(msg.getTime(), msg.getSeckillId(),
                msg.getToken(), null, "订单创建成功~", Result.SUCCESS_CODE);
        String topic = MQConstant.ORDER_RESULT_SUCCESS_DEST;
        try {
            String orderNo = orderInfoService.createOrder(msg.getUserPhone(), msg.getSeckillId());
            // 订单创建成功
            log.info("[创建订单监听器] 订单创建成功，订单编号：{}，即将通知订单创建结果...", orderNo);

            // 创建订单结果对象
            result.setOrderNo(orderNo);
        } catch (Exception e) {
            log.error("[创建订单监听器] 订单下单失败...", e);
            // 订单创建失败
            result.setMsg(SeckillCodeMsg.SECKILL_ERROR.getMsg());
            result.setCode(SeckillCodeMsg.SECKILL_ERROR.getCode());
            topic = MQConstant.ORDER_RESULT_FAIL_DEST;
        } finally {
            // 发送消息
            rocketMQTemplate.asyncSend(topic, result, new DefaultSendCallback("订单创建结果", result));
        }
    }
}
