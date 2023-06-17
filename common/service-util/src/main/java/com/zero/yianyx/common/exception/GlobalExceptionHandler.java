package com.zero.yianyx.common.exception;

import com.zero.yianyx.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description: 全局异常处理类
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */

/**
 * @author ZeroYiAn
 *
 * @ControllerAdvice 原理：AOP 面向切面编程实现一个增强的 Controller。使用这个 Controller ，可以实现三个方面的功能：
 *
 * 1.全局异常处理 结合注解 @ExceptionHandler 使用
 * 2。全局数据绑定 @ModelAttribute
 * 3.全局数据预处理 @ModelAttribute ,@InitBinde
 * 使用@ControllerAdvice来声明一些全局性的东西，@ControllerAdvice+@ExceptionHandler
 * @ExceptionHandler 注解标注的方法：用于捕获Controller中抛出的不同类型的异常，从而达到异常全局处理的目的；
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail(null);
    }

    /**
     * 自定义异常处理方法
     * @param e  自定义异常处理类
     * @return   失败结果信息
     */
    @ExceptionHandler(YianyxException.class)
    @ResponseBody
    public  Result error(YianyxException e){

        return Result.build(null,e.getCode(),e.getMessage());
    }

}
