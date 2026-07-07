package com.mj.aiknowledgebase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Document;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
 * <p>
 * 文档表 服务类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
public interface IDocumentService extends IService<Document> {

    Result<?> add(Document document);

    Page<Document> listDocuments(Long knowledgeId, LocalDateTime startTime, LocalDateTime endTime, Integer pageNum, Integer pageSize);

    Result<?> delete(Long id);
}
