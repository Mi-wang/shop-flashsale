package cn.wolfcode.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;

/**
 * @author wby
 * @version 1.0
 * @date 2023-02-18 018 20:03
 */

@FeignClient("pay-service")
public interface AlipayFeignApi {

    @PostMapping("/alipay/prepay")
    Result<String> prepay(@RequestBody PayVo vo);

    @PostMapping("/alipay/checkSignature")
    Result<Boolean> checkSignature(@RequestBody HashMap<String, String> params);
}