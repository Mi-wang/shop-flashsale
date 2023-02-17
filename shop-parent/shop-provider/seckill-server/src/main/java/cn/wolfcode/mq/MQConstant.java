package cn.wolfcode.mq;


public class MQConstant {
    //订单队列
    public static final String ORDER_PENDING_TOPIC = "ORDER_PENDING_TOPIC";
    public static final String ORDER_PENDING_GROUP = "ORDER_PENDING_GROUP";
    public static final String ORDER_PENDING_TAG = "CREATE";
    public static final String ORDER_PENDING_DEST = ORDER_PENDING_TOPIC + ":" + ORDER_PENDING_TAG;
    //订单结果
    public static final String ORDER_RESULT_TOPIC = "ORDER_RESULT_TOPIC";
    //订单超时取消
    public static final String ORDER_PAY_TIMEOUT_TOPIC = "ORDER_PAY_TIMEOUT_TOPIC";
    //取消本地标识
    public static final String CANCEL_SECKILL_OVER_SIGN_TOPIC = "CANCEL_SECKILL_OVER_SIGN_TOPIC";
    //订单创建成功Tag
    public static final String ORDER_RESULT_SUCCESS_TAG = "SUCCESS";
    public static final String ORDER_RESULT_SUCCESS_DEST = ORDER_RESULT_TOPIC + ":" + ORDER_RESULT_SUCCESS_TAG;
    //订单创建成失败Tag
    public static final String ORDER_RESULT_FAIL_TAG = "FAIL";
    public static final String ORDER_RESULT_FAIL_DEST = ORDER_RESULT_TOPIC + ":" + ORDER_RESULT_FAIL_TAG;
    //延迟消息等级
    public static final int ORDER_PAY_TIMEOUT_DELAY_LEVEL = 13;
}
