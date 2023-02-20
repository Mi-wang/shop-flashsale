package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.callback.DefaultSendCallback;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.controller.OrderInfoController;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

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
    public String createOrder(Long userId, Long seckillId) {
        // 创建锁对象
        RLock lock = redissonClient.getLock("seckill:product:stock:lock:" + seckillId);
        SeckillProductVo vo = null;
        try {
            // 加锁
            lock.lock(10, TimeUnit.SECONDS);
            // 查询最新的库存情况
            vo = seckillProductService.findById(seckillId);
            // 库存大于 0 才扣库存
            if (vo.getStockCount() > 0) {
                // 扣除库存
                int row = seckillProductService.decrStockCount(seckillId);
                if (row <= 0) {
                    // 乐观锁成功，库存数不足
                    throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
                }
            }
            // 创建订单对象
            OrderInfo orderInfo = this.create(userId, vo);

            // 保存订单对象
            orderInfoMapper.insert(orderInfo);

            return orderInfo.getOrderNo();
        } catch (BusinessException be) {
            // 不处理继续往外抛出
            throw be;
        } catch (Exception e) {
            // 重新同步 redis 库存，设置本地库存售完标识为 false
            if (vo != null && vo.getStockCount() > 0) {
                this.rollbackStockCount(vo);
            }
            log.error("[创建订单] 创建订单失败：", e);

            // 继续向外抛出异常
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    @Override
    public void checkOrderTimeout(String orderNo, Long seckillId) {
        //  基于订单编号查询订单对象
        //  检查订单是否已经支付，如果已支付，直接忽略
        //  修改订单状态为超时取消
        int row = orderInfoMapper.updateCancelStatus(orderNo, OrderInfo.STATUS_TIMEOUT);
        if (row > 0) {
            //  回滚库存（mysql/redis）
            // +1
            seckillProductService.incrStockCount(seckillId);
            SeckillProductVo vo = seckillProductService.findById(seckillId);
            //  取消库存售完标记（本地标识 JVM） => 分布式缓存同步 => 广播模式
            rollbackStockCount(vo);
        }
    }

    private void rollbackStockCount(SeckillProduct sp) {
        // redis 库存回补
        redisTemplate.opsForHash().put(SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(sp.getTime() + ""),
                sp.getId() + "", sp.getStockCount() + "");
        // 取消本地标识
        rocketMQTemplate.asyncSend(MQConstant.CANCEL_SECKILL_OVER_SIGN_TOPIC, sp.getId(), new DefaultSendCallback("取消本地标识", sp.getId()));
        log.warn("[回滚库存] 订单创建失败，回补 redis 库存以及取消本地售完标记");
    }

    private OrderInfo create(Long userId, SeckillProductVo vo) {
        Date date = new Date();
        OrderInfo orderInfo = new OrderInfo();
        // 雪花算法生成分布式唯一 id
        orderInfo.setOrderNo(IdGenerateUtil.get().nextId() + "");
        orderInfo.setCreateDate(date);
        // 积分
        orderInfo.setIntergral(vo.getIntergral());
        // 收货地址
        orderInfo.setDeliveryAddrId(1L);
        // 商品数量
        orderInfo.setProductCount(1);
        orderInfo.setProductId(vo.getProductId());
        orderInfo.setProductImg(vo.getProductImg());
        orderInfo.setProductName(vo.getProductName());
        orderInfo.setProductPrice(vo.getProductPrice());
        orderInfo.setSeckillDate(date);
        orderInfo.setSeckillId(vo.getId());
        orderInfo.setSeckillPrice(vo.getSeckillPrice());
        orderInfo.setSeckillTime(vo.getTime());
        orderInfo.setStatus(OrderInfo.STATUS_ARREARAGE);
        orderInfo.setUserId(userId);
        return orderInfo;
    }
}
