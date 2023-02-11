package cn.wolfcode.mq;


public class MQConstant {
    //订单队列
    public static final String ORDER_PEDDING_TOPIC = "ORDER_PEDDING_TOPIC";
    //订单结果
    public static final String ORDER_RESULT_TOPIC = "ORDER_RESULT_TOPIC";
    //订单超时取消
    public static final String ORDER_PAY_TIMEOUT_TOPIC = "ORDER_PAY_TIMEOUT_TOPIC";
    //取消本地标识
    public static final String CANCEL_SECKILL_OVER_SIGE_TOPIC = "CANCEL_SECKILL_OVER_SIGE_TOPIC";
    //订单创建成功Tag
    public static final String ORDER_RESULT_SUCCESS_TAG = "SUCCESS";
    //订单创建成失败Tag
    public static final String ORDER_RESULT_FAIL_TAG = "FAIL";
    //延迟消息等级
    public static final int ORDER_PAY_TIMEOUT_DELAY_LEVEL = 13;
}
