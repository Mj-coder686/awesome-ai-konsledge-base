package com.mj.aiknowledgebase.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Knowledge;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 知识库表 服务类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
public interface IKnowledgeService extends IService<Knowledge> {

    Result<?> add(Knowledge knowledge);

    Result<?> delete(Long id);

    Page<Knowledge> listKnowLedge(Integer pageNum, Integer pageSize);
}
