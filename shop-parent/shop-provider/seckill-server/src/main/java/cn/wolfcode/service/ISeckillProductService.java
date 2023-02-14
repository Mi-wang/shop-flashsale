package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;
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
}
