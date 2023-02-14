package cn.wolfcode.job;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.SeckillFeignApi;
import cn.wolfcode.redis.JobRedisKey;
import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-14 014 11:36
 */
@Component
@Setter
@Getter
@RefreshScope
@Slf4j
public class InitSeckillProductJob implements SimpleJob {

    @Value("${jobCron.initSeckillProduct}")
    private String cron;

    @Autowired
    private SeckillFeignApi seckillFeignApi;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void execute(ShardingContext ctx) {
        // 初始化对应场次的秒杀商品信息
        int[] times = {10, 12, 14};
        for (int time : times) {
            doWork(time);
        }
    }

    public void doWork(Integer time) {
        // Redis 结构：String
        // Redis Key：seckill:products:init:string:10 => values
        // 1. 查询今天的秒杀商品列表
        Result<List<SeckillProductVo>> result = seckillFeignApi.queryByTime(time);
        if (result.hasError()) {
            log.warn("[初始化商品任务] 未查询到今天的秒杀数据: {}", JSON.toJSONString(result));
            return;
        }

        List<SeckillProductVo> list = result.getData();
        log.info("[初始化商品任务] 准备开始初始化 {} 场次商品列表，查询到：size={}",
                time, list.size());
        String hashRealKey = JobRedisKey.INIT_SECKILL_PRODUCT_DETAIL_HASH.getRealKey(time + "");
        for (SeckillProductVo vo : list) {
            // 存入 Hash 结构
            redisTemplate.opsForHash().put(hashRealKey, vo.getId() + "", JSON.toJSONString(vo));
        }
        // 2. 以场次作为唯一 key 把该场次对应的列表数据存入 Redis
        redisTemplate.opsForValue().set(JobRedisKey.INIT_SECKILL_PRODUCT_LIST_STRING.getRealKey(time + ""),
                JSON.toJSONString(list));
        log.info("[初始化商品任务] 准备开始初始化数据完成......");
    }
}
