package cn.wolfcode.web.feign;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("uaa-service")
public interface UserInfoFeignApi {

    @GetMapping("/users/{phone}")
    Result<UserInfo> getByPhone(@PathVariable Long phone);
}
