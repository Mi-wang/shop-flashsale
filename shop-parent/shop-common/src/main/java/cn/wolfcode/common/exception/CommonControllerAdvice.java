package cn.wolfcode.common.exception;

import cn.wolfcode.common.web.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wolfcode
 */
public class CommonControllerAdvice {
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result handleBusinessException(BusinessException ex) {
        return Result.error(ex.getCodeMsg());
    }
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Result> handleDefaultException(Exception ex) {
        ex.printStackTrace();//在控制台打印错误消息.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.defaultError());
    }
}
