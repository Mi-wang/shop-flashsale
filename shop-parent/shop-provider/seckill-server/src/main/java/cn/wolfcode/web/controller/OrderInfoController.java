package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;

/*    @GetMapping("/{id}")
    public Result<OrderInfo> getById(@PathVariable String id) {
        return Result.success(orderInfoService.getById(id));
    }*/

    @RequireLogin
    @PostMapping("/doSeckill")
    public Result<String> doSeckill(Long seckillId, Integer time, @RequestHeader(CommonConstants.TOKEN_NAME) String token) {
        // 1. 检查用户是否登录，获取登录用户信息
        UserInfo userInfo = this.getByToken(token);
        if (userInfo == null) {
            throw new BusinessException(CommonCodeMsg.TOKEN_INVALID);
        }
        // 2. 基于秒杀商品 id 查询秒杀商品对象
        SeckillProductVo vo = seckillProductService.findById(seckillId);
        // 3. 当前时间是否处于秒杀活动时间范围内
        if (!validTime(vo.getStartDate(), vo.getTime())) {
            throw new BusinessException(SeckillCodeMsg.INVALID_TIME_ERROR);
        }
        // 4. 查询当前用户是否已经下过单
        /*OrderInfo orderInfo = orderInfoService.getByUserIdAndSeckillId(userInfo.getPhone(), seckillId);
        if (orderInfo != null) {
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        }*/
        // 5. 判断库存是否足够 > 0
        if (vo.getStockCount() <= 0) {
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        // 6. 进行下单操作(库存数量 -1, 创建秒杀订单)
        return Result.success(orderInfoService.createOrder(userInfo.getPhone(), vo));
    }


    private boolean validTime(Date startDate, Integer time) {
        Calendar calendar = Calendar.getInstance();
        // 设置活动的开始日期
        calendar.setTime(startDate);
        // 设置当前场次的小时数
        calendar.set(Calendar.HOUR_OF_DAY, time);
        // 分钟
        calendar.set(Calendar.MINUTE, 0);
        // 秒
        calendar.set(Calendar.SECOND, 0);

        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        Date endtime = calendar.getTime();

        long now = System.currentTimeMillis();
        return true;
        /*return startTime.getTime() <= now && endtime.getTime() > now;*/
    }

    private UserInfo getByToken(String token) {
        String strObj = redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token));
        if (StringUtils.isEmpty(strObj)) {
            return null;
        }
        return JSON.parseObject(strObj, UserInfo.class);
    }

    @GetMapping("/find")
    public Result<OrderInfo> getById(String orderNo) {
        return Result.success(orderInfoService.getById(orderNo));
    }
}
