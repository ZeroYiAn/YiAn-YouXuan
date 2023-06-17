package com.zero.yianyx.product.repository;

import com.zero.yianyx.model.search.SkuEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */

public interface SkuESRepository extends ElasticsearchRepository<SkuEs, Long> {
}
