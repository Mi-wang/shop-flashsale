package cn.wolfcode.web.controller;


import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.feign.AlipayFeignApi;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/orderPay")
@RefreshScope
public class OrderPayController {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private AlipayFeignApi alipayFeignApi;

    @Value("{pay.returnUrl}")
    private String returnUrl;
    @Value("{pay.notifyUrl}")
    private String notifyUrl;

    @GetMapping("/return_url")
    public String returnUrl(HashMap<String, String> params) {
        System.out.println(params);

        // 远程调用支付服务验证签名
        Result<Boolean> result = alipayFeignApi.checkSignature(params);
        if (result.hasError()) {
            throw new BusinessException(new CodeMsg(result.getCode(), result.getMsg()));
        }

        boolean signVerified = result.getData();
        if (signVerified) {
            //商户订单号
            String outTradeNo = params.get("out_trade_no");

            // 验证签名成功
            return "redirect:http://localhost/order_detail.html?orderNo=" + outTradeNo;
        }

        // 验证签名失败
        return "redirect:https://www.wolfcode.cn";
    }

    @PostMapping("/notify_url")
    public void notifyUrl(HashMap<String, String> params) {

    }
    @GetMapping("/alipay")
    public Result<String> prepay(String orderNo, Integer type) {
        // 查询订单
        OrderInfo orderInfo = orderInfoService.getById(orderNo);
        if (orderInfo == null) {
            throw new BusinessException(SeckillCodeMsg.ORDER_NOT_EXISTS_ERROR);
        }
        // 构建支付对象
        PayVo vo = this.buildPayRequest(orderInfo);
        // 远程调用支付服务发起预支付请求
        return alipayFeignApi.prepay(vo);
    }

    private PayVo buildPayRequest(OrderInfo orderInfo) {
        PayVo vo = new PayVo();
        vo.setBody("秒杀活动 " + orderInfo.getSeckillTime() + " 场次成功秒杀1个商品");
        vo.setSubject(orderInfo.getProductName());
        vo.setNotifyUrl(notifyUrl);
        vo.setReturnUrl(returnUrl);
        vo.setTotalAmount(orderInfo.getSeckillPrice().toString());
        vo.setOutTradeNo(orderInfo.getOrderNo());
        return vo;
    }
}
