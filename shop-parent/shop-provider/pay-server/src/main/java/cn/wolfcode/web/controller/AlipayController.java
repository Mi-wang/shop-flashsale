package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.web.msg.PayCodeMsg;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;

    @PostMapping("/prepay")
    public Result prepay(@RequestBody PayVo vo) {

        //请求
        try {
            // 创建支付宝支付请求对象
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setReturnUrl(vo.getReturnUrl());
            alipayRequest.setNotifyUrl(vo.getNotifyUrl());

            //商户订单号，商户网站订单系统中唯一订单号，必填
            String out_trade_no = vo.getOutTradeNo();
            //付款金额，必填
            String total_amount = vo.getTotalAmount();
            //订单名称，必填
            String subject = vo.getSubject();
            //商品描述，可空
            String body = vo.getBody();

            // 设置支付请求参数
            alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                    + "\"total_amount\":\"" + total_amount + "\","
                    + "\"subject\":\"" + subject + "\","
                    + "\"body\":\"" + body + "\","
                    + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");


            String result = alipayClient.pageExecute(alipayRequest).getBody();
            System.out.println(result);
            // result = 支付宝返回的 html 片段
            // <form action="支付宝的登录页">....
            // <script>document.forms[0].submit</script>
            return Result.success(result);
        } catch (AlipayApiException e) {
            log.error("[支付宝支付] 参数签名失败，请检查商户私钥以及支付宝公钥配置....", e);
            return Result.error(PayCodeMsg.ALIPAY_SIGNATURE_FAILED);
        }
    }
}
