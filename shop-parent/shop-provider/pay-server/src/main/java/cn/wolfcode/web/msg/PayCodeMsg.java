package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode
 */
public class PayCodeMsg extends CodeMsg {
    private PayCodeMsg(Integer code, String msg){
        super(code,msg);
    }
    public static final PayCodeMsg ALIPAY_SIGNATURE_FAILED = new PayCodeMsg(501002, "支付宝签名失败");
}
