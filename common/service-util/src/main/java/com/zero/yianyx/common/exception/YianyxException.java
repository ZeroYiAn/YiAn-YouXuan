package com.zero.yianyx.common.exception;

import com.zero.yianyx.common.result.ResultCodeEnum;
import lombok.Data;

/**
 * @description: 自定义异常处理类
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Data
public class YianyxException extends RuntimeException {
    /**
     * 异常状态码
     */
    private Integer code;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param message
     * @param code
     */
    public YianyxException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * 接受枚举类型对象
     * @param resultCodeEnum
     */
    public YianyxException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "YiAnYouXuanException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }

}
