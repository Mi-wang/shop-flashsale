package cn.wolfcode.service;


import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;

import java.util.Map;

/**
 * Created by wolfcode
 */
public interface IOrderInfoService {

    OrderInfo getById(String id);

     OrderInfo getByUserIdAndSeckillId(Long phone, Long seckillId);

    String createOrder(Long userId, SeckillProductVo vo);
}
