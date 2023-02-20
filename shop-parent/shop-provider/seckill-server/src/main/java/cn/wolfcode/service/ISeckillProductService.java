package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProductVo;

import java.util.List;


public interface ISeckillProductService {
    /**
     * 基于秒杀场次查询今日秒杀商品列表
     *
     * @param time 秒杀场次
     * @return 秒杀商品列表
     */
    List<SeckillProductVo> queryByTime(Integer time);

    List<SeckillProductVo> queryByTimeInCache(Integer time);

    /**
     * 基于秒杀 id 查询秒杀商品对象
     *
     * @param seckillId 秒杀 id
     * @return 返回的秒杀商品对象
     */
    SeckillProductVo findById(Long seckillId);

    SeckillProductVo findByIdInCache(Long seckillId, Integer time);

    int decrStockCount(Long id);

    void incrStockCount(Long seckillId);
}
