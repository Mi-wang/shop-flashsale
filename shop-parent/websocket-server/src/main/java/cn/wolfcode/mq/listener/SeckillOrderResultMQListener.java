package cn.wolfcode.mq.listener;

import cn.wolfcode.core.WebSocketServer;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.TimeUnit;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-17 017 21:13
 */
@Slf4j
@RocketMQMessageListener(
        topic = MQConstant.ORDER_RESULT_TOPIC,
        selectorExpression = "*",
        consumerGroup = "ORDER_RESULT_GROUP"
)
@Component
public class SeckillOrderResultMQListener implements RocketMQListener<OrderMQResult> {

    @Override
    public void onMessage(OrderMQResult message) {
        String result = JSON.toJSONString(message);
        log.info("[订单结果监听器] 收到订单创建结果：{}，准备通知前端用户...", result);

        // 通过 websocket 通知前端用户
        try {
            int count = 3;
            do {
                Session session = WebSocketServer.CLIENTS.get(message.getToken());
                if (session != null) {
                    // 发送消息
                    session.getBasicRemote().sendText(result);
                    log.info("[订单结果监听器] 通知前端用户{}成功......", message.getToken());
                    break;
                }
                count--;
                log.warn("[订单结果监听器] 暂时无法获得前端用户：{}，30ms 后继续重试，剩余：{}", message.getToken(), count);
                TimeUnit.SECONDS.sleep(5);
            } while (count > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
