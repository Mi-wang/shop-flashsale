package cn.wolfcode.service.impl;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.Product;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by wolfcode
 */
@Slf4j
@Service
public class OrderInfoSeviceImpl implements IOrderInfoService {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;

    @Autowired
    private ProductFeignApi productFeignApi;

    @Override
    public OrderInfo getById(String id) {
        OrderInfo info = orderInfoMapper.find(id);
        Result<Product> result = productFeignApi.getById(info.getProductId());
        String json = JSON.toJSONString(result);

        if (result.hasError()) {
            log.error("订单服务查询商品信息失败：{}", json);
            return null;
        }

        log.info("查询订单信息：{}", JSON.toJSONString(info));
        return info;
    }
}
