package com.mj.aiknowledgebase.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Knowledge;
import com.mj.aiknowledgebase.service.IKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 知识库表 前端控制器
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Autowired
    private IKnowledgeService knowledgeService;


    @PostMapping("/add")
    public Result<?> add(@RequestBody Knowledge knowledge) {
       return knowledgeService.add(knowledge);
    }

    @GetMapping("/list")
    public Result<Page<Knowledge>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(knowledgeService.listKnowLedge(pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return knowledgeService.delete(id);
    }

    @GetMapping("/listAll")
    public Result<List<Knowledge>> listAll() {
        return Result.success(knowledgeService.list());
    }

}
