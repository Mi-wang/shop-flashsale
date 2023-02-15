package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
        log.info("查询订单信息：{}", id);
        return orderInfoMapper.find(id);
    }

    @Override
    public OrderInfo getByUserIdAndSeckillId(Long phone, Long seckillId) {
        return orderInfoMapper.getByUserIdAndSeckillId(phone, seckillId);
    }

    /**
     * 测试参数：
     * 线程：100
     * 次数：5000
     * 数据：
     * TPS：89/s
     * 异常比例：1.92%
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createOrder(Long userId, SeckillProductVo vo) {
        // 扣除库存
        int row = seckillProductService.decrStockCount(vo.getId());
        if (row <= 0) {
            // 乐观锁成功，库存数不足
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        // 创建订单对象
        OrderInfo orderInfo = this.create(userId, vo);
        // 保存订单对象
        orderInfoMapper.insert(orderInfo);
        return orderInfo.getOrderNo();
    }

    private OrderInfo create(Long userId, SeckillProductVo vo) {
        Date date = new Date();
        OrderInfo orderInfo = new OrderInfo();
        // 雪花算法生成分布式唯一 id
        orderInfo.setOrderNo(IdGenerateUtil.get().nextId() + "");
        orderInfo.setCreateDate(date);
        // 收获地址
        orderInfo.setDeliveryAddrId(1L);
        // 商品数量
        orderInfo.setProductCount(1);
        // 积分
        orderInfo.setIntergral(vo.getIntergral());
        orderInfo.setProductId(vo.getProductId());
        orderInfo.setProductImg(vo.getProductImg());
        orderInfo.setProductName(vo.getProductName());
        orderInfo.setProductPrice(vo.getProductPrice());
        orderInfo.setUserId(userId);
        orderInfo.setSeckillDate(date);
        orderInfo.setSeckillId(vo.getId());
        orderInfo.setSeckillTime(vo.getTime());
        orderInfo.setStatus(OrderInfo.STATUS_ARREARAGE);
        orderInfo.setSeckillPrice(vo.getSeckillPrice());
        return orderInfo;
    }
}
