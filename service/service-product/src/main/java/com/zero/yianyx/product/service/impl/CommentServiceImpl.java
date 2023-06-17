package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.Comment;
import com.zero.yianyx.product.mapper.CommentMapper;
import com.zero.yianyx.product.service.CommentService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
