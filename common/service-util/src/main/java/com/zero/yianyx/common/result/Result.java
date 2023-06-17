package com.zero.yianyx.common.result;

import lombok.Data;

/**
 * @description: 统一返回结果类
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;
    /**
     * 信息
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    private Result() { }

    /**
     * 设置数据,返回对象的方法
     */
    public static<T> Result<T> build(T data,Integer code,String message) {
        //创建Resullt对象，设置值，返回对象
        Result<T> result = new Result<>();
        //判断返回结果中是否需要数据
        if(data != null) {
            //设置数据到result对象
            result.setData(data);
        }
        //设置其他值
        result.setCode(code);
        result.setMessage(message);
        //返回设置值之后的对象
        return result;
    }
    public static<T> Result<T> ok() {
        return build(null,ResultCodeEnum.SUCCESS.getCode(),ResultCodeEnum.SUCCESS.getMessage());
    }


    public static<T> Result<T> ok(T data) {
        Result<T> result = build(data, ResultCodeEnum.SUCCESS.getCode(),ResultCodeEnum.SUCCESS.getMessage());
        return result;
    }

    public static<T> Result<T> fail(T data) {
        return build(data,ResultCodeEnum.FAIL.getCode(),ResultCodeEnum.FAIL.getMessage());
    }
}
