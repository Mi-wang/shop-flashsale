package cn.wolfcode.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


/**
 * @author 20463
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderMQResult implements Serializable {
    private Integer time;//秒杀场次
    private Long seckillId;//秒杀商品id
    private String token;//用户token
    private String orderNo;//订单编号
    private String msg;//提示消息
    private Integer code;//状态码
}
