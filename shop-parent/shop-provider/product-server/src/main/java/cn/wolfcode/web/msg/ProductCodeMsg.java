package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode
 */
public class ProductCodeMsg extends CodeMsg {
    private ProductCodeMsg(Integer code, String msg){
        super(code,msg);
    }
}
