package cn.wolfcode.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("seckill-service")
public interface SeckillFeignApi {

    @GetMapping("/seckillProduct/queryByTime")
    Result<List<SeckillProductVo>> queryByTime(@RequestParam Integer time);
}