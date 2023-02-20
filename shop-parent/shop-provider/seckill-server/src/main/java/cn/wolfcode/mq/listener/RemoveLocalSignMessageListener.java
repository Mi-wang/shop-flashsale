package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.web.controller.OrderInfoController;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-20 020 8:55
 */
@RocketMQMessageListener(
        consumerGroup = "REMOVE_LOCAL_SIGN_GROUP",
        topic = MQConstant.CANCEL_SECKILL_OVER_SIGN_TOPIC,
        // 消息模式修改为广播模式
        messageModel = MessageModel.BROADCASTING
)
@Component
@Slf4j
public class RemoveLocalSignMessageListener implements RocketMQListener<Integer> {

    @Override
    public void onMessage(Integer seckillId) {
        log.info("[取消本地标识监听器] 正在准备取消：{} 商品的本地售完标记...", seckillId);
        OrderInfoController.LOCAL_STOCK_OVER_FALG_MAP.put(seckillId.longValue(), false);
    }
}
