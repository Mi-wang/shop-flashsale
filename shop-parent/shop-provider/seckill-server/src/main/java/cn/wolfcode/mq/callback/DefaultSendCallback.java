package cn.wolfcode.mq.callback;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-17 017 20:57
 */
@Slf4j
public class DefaultSendCallback implements SendCallback {

    private String tag;
    private Object data;

    public DefaultSendCallback(String tag, Object data) {
        this.tag = tag;
        this.data = data;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        log.info("[{}] 发送消息成功: msgId={}, status={}", tag, sendResult.getMsgId(), sendResult.getSendStatus());
    }

    @Override
    public void onException(Throwable throwable) {
        log.warn("[{}] 消息发送失败，出现异常，请及时处理消息: message={}", tag, JSON.toJSONString(data));
        log.error("[" + tag + "] 消息发送失败", throwable);
    }
}
