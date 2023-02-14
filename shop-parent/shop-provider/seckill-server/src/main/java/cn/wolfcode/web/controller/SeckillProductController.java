package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.service.ISeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/seckillProduct")
@Slf4j
public class SeckillProductController {
    @Autowired
    private ISeckillProductService seckillProductService;

    @GetMapping("/queryByTime")
    public Result<List<SeckillProductVo>> queryByTime(Integer time) {
        return Result.success(seckillProductService.queryByTimeInCache(time));
    }

    @GetMapping("/find")
    public Result<SeckillProductVo> findById(Long seckillId, Integer time) {
        return Result.success(seckillProductService.findByIdInCache(seckillId, time));
    }
}
