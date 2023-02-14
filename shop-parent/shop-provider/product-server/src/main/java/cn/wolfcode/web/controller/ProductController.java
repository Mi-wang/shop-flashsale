package cn.wolfcode.web.controller;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.service.IProductService;
import cn.wolfcode.web.feign.UserInfoFeignApi;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private IProductService productService;
    @Autowired
    private UserInfoFeignApi userInfoFeignApi;

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Result<UserInfo> result = userInfoFeignApi.getByPhone(13088889999L);
        String json = JSON.toJSONString(result);
        if (result.hasError()) {
            log.error("查询用户信息失败：{}", json);
            return Result.error(new CodeMsg(result.getCode(), result.getMsg()));
        }
        log.info("商品服务查询用户信息：{}", json);
        return Result.success(productService.getById(id));
    }
}
