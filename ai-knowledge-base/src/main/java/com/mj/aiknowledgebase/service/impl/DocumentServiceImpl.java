package com.mj.aiknowledgebase.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Document;
import com.mj.aiknowledgebase.mapper.DocumentMapper;
import com.mj.aiknowledgebase.service.IDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.aiknowledgebase.util.UserContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 文档表 服务实现类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements IDocumentService {

    @Override
    public Result<?> add(Document document) {
        if (document == null || document.getKnowledgeId() == null) {
            return Result.fail("参数错误");
        }
        boolean save = save(document);
        return save ? Result.success() : Result.fail("添加失败");
    }

    @Override
    public Page<Document> listDocuments(Long knowledgeId, LocalDateTime startTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Document> page = new Page<>(pageNum, pageSize);
        return lambdaQuery()
                .eq(knowledgeId != null, Document::getKnowledgeId, knowledgeId)
                .ge(startTime != null, Document::getCreateTime, startTime)
                .le(endTime != null, Document::getCreateTime, endTime)
                .orderByDesc(Document::getCreateTime)
                .page(page);
    }

    @Override
    public Result<?> delete(Long id) {
        if (id == null) {
            return Result.fail("参数错误");
        }
        Document one = getById(id);
        if (one == null) {
            return Result.fail("文档不存在");
        }
        return removeById(id) ? Result.success() : Result.fail("删除失败");
    }
}
