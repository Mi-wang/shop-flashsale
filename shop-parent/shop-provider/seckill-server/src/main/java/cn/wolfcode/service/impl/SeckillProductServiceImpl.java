package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SeckillProductServiceImpl implements ISeckillProductService {
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ProductFeignApi productFeignApi;

    @Override
    public List<SeckillProductVo> queryByTime(Integer time) {
        log.info("[秒杀商品] 查询秒杀商品列表: time={}......", time);
        // 基于当前日期 + 场次从数据库中查询所有秒杀商品信息
        List<SeckillProduct> seckillProducts = seckillProductMapper.queryCurrentlySeckillProduct(time);
        // 如果没有数据就直接返回空集合
        if (seckillProducts == null || seckillProducts.size() == 0) {
            return Collections.emptyList();
        }
        // 遍历秒杀商品列表，得到商品 id 列表
        List<Long> productIdList = seckillProducts.stream()
                .map(SeckillProduct::getProductId)
                .collect(Collectors.toList());
        // 远程调用商品服务，基于商品 id 列表查询商品列表
        Result<List<Product>> productResult = productFeignApi.queryByIdList(productIdList);
        if (productResult.hasError()) {
            log.error("[秒杀商品] 查询商品服务失败，参数：{}，返回：{}", productIdList, productResult);
            throw new BusinessException(new CodeMsg(productResult.getCode(), productResult.getMsg()));
        }
        List<Product> products = productResult.getData();
        // key=商品id，value=商品对象
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        // 遍历秒杀商品列表
        // 得到当前秒杀商品对象对应的商品对象
        // 封装秒杀商品vo 对象并存入最终返回的集合
        // 返回秒杀商品vo 集合
        return seckillProducts.stream().map(sp -> {
            SeckillProductVo productVo = new SeckillProductVo();
            Product product = productMap.get(sp.getProductId());
            // 将商品对象中的信息拷贝到 vo 中
            BeanUtils.copyProperties(product, productVo);
            BeanUtils.copyProperties(sp, productVo);

            return productVo;
        }).collect(Collectors.toList());
    }

    /**
     * 测试报告：
     * 吞吐量：2660/s
     */
    @Override
    public List<SeckillProductVo> queryByTimeInCache(Integer time) {
        String realKey = SeckillRedisKey.INIT_SECKILL_PRODUCT_LIST_STRING.getRealKey(time + "");
        log.info("[秒杀商品] 从 redis 查询数据：key={}", realKey);
        String json = redisTemplate.opsForValue().get(realKey);
        if (StringUtils.isEmpty(json)) {
            // 如果 redis 中数据为空，从数据库中查询
            List<SeckillProductVo> list = queryByTime(time);
            if (list != null && list.size() > 0) {
                log.info("[秒杀商品] redis 秒杀商品列表不存在，查询数据库并存入 redis：size={}", list.size());
                // 将从数据库中查询出的数据重新存入 redis
                redisTemplate.opsForValue().set(realKey, JSON.toJSONString(list));
                // 返回查询结果
                return list;
            }
        }
        return JSON.parseArray(json, SeckillProductVo.class);
    }

    /**
     * 测试数据：100 线程执行 500 次
     * 性能测试（QPS）：500/s
     * 异常比例：6.3
     */
    @Override
    public SeckillProductVo findById(Long seckillId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillId);
        log.info("[秒杀商品] 查询秒杀商品对象：{}", seckillId);
        // 查询秒杀商品对应的商品对象
        Result<Product> productResult = productFeignApi.getById(sp.getProductId());
        if (productResult.hasError()) {
            log.error("[秒杀商品] 查询商品服务失败，参数：{}，返回：{}", sp.getProductId(), productResult);
            throw new BusinessException(new CodeMsg(productResult.getCode(), productResult.getMsg()));
        }
        Product product = productResult.getData();
        // 将商品对象的信息拷贝到 vo 中
        SeckillProductVo vo = new SeckillProductVo();
        BeanUtils.copyProperties(product, vo);
        BeanUtils.copyProperties(sp, vo);
        return vo;
    }


    /**
     * 测试数据：100 线程执行 500 次
     * 性能测试（QPS）：1045/s
     * 异常比例：0
     */
    @Override
    public SeckillProductVo findByIdInCache(Long seckillId, Integer time) {
        // 从 redis 中查询秒杀商品数据
        String realKey = SeckillRedisKey.INIT_SECKILL_PRODUCT_DETAIL_HASH.getRealKey(time + "");
        log.info("[秒杀商品] 查询秒杀商品详情：time={}, seckillId={}", time, seckillId);
        String json = (String) redisTemplate.opsForHash().get(realKey, seckillId + "");
        if (StringUtils.isEmpty(json)) {
            // 如果 redis 查不到数据，就从数据库查询
            SeckillProductVo vo = findById(seckillId);
            if (vo != null) {
                log.info("[秒杀商品] redis 数据不存在，查询数据库：seckillId={}", seckillId);
                // 存入 redis
                redisTemplate.opsForHash().put(realKey, seckillId + "", JSON.toJSONString(vo));
                return vo;
            }
        }
        return JSON.parseObject(json, SeckillProductVo.class);
    }

    @Override
    public int decrStockCount(Long id) {
        String key = "seckill:product:" + id;
        // 加分布式锁
        String clientId = "";
        try {
            // 由于 SpringRedisTemplate 的 setIfAbsent 同时设置超时时间的重载方法没有使用 setnx 命令，因此也是不是原子性的问题，依然可能设置超时时间失败
            // 完善的解决方案：lua 脚本 =》 Redis 批处理命令
            clientId = IdGenerateUtil.get().nextId() + "";
            Boolean ret = redisTemplate.opsForValue().setIfAbsent(key, clientId, 10, TimeUnit.SECONDS);
            if (ret == null || !ret) {
                return 0;
            }

            // 再次检查库存是否足够
            SeckillProduct sp = seckillProductMapper.selectById(id);
            if (sp.getStockCount() > 0) {
                return seckillProductMapper.decrStock(id);
            }
        } finally {
            // 确认是否是自己加的锁
            if (clientId.equals(redisTemplate.opsForValue().get(key))) {
                // 只有自己加的锁才可以删除
                redisTemplate.delete(key);
            }
        }
        return 0;
    }
}
