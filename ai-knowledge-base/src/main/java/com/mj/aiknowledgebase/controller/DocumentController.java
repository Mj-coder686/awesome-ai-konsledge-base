package com.mj.aiknowledgebase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Document;
import com.mj.aiknowledgebase.service.IDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 文档表 前端控制器
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@RestController
@RequestMapping("/document")
public class DocumentController {
    @Autowired
    private IDocumentService documentService;

    @PostMapping
    public Result<?> add(@RequestBody Document document) {
        return documentService.add(document);
    }

    @GetMapping("/list")
    public Result<Page<Document>> list(
            @RequestParam(required = false) Long knowledgeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(documentService.listDocuments(knowledgeId, startTime, endTime, pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return documentService.delete(id);
    }
}
