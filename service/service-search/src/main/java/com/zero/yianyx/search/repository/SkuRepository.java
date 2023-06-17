package com.zero.yianyx.search.repository;

import com.zero.yianyx.model.search.SkuEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */

public interface SkuRepository extends ElasticsearchRepository<SkuEs, Long> {

    /**
     * 获取爆款商品 SpringData会根据接口命名规范自动生成sql进行查询
     * @param pageable
     * @return
     */
    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);

    Page<SkuEs> findByCategoryIdAndWareId(Long categoryId, Long wareId, Pageable pageable);

    Page<SkuEs> findByKeywordAndWareId(String keyword, Long wareId, Pageable pageable);
}
