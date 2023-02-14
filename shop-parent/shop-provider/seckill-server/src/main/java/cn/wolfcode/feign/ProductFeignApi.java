package cn.wolfcode.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("product-service")
public interface ProductFeignApi {

    @GetMapping("/product/{id}")
    Result<Product> getById(@PathVariable Long id);

    @GetMapping("/product/queryByIdList")
    Result<List<Product>> queryByIdList(@RequestParam List<Long> idList);
}
