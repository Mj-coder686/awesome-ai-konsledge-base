package com.mj.aiknowledgebase.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mj.aiknowledgebase.common.Result;
import com.mj.aiknowledgebase.domain.po.Knowledge;
import com.mj.aiknowledgebase.mapper.KnowledgeMapper;
import com.mj.aiknowledgebase.service.IKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.aiknowledgebase.util.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 知识库表 服务实现类
 * </p>
 *
 * @author MJ
 * @since 2026-07-04
 */
@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeMapper, Knowledge> implements IKnowledgeService {

    @Override
    public Result<?> add(Knowledge knowledge) {
        if (knowledge == null){
            return Result.fail("参数错误");
        }
        Long userId = UserContext.getUserId();
        Knowledge one = lambdaQuery().eq(Knowledge::getName, knowledge.getName())
                .eq(Knowledge::getUserId, userId)
                .one();
        if (one != null){
            return Result.fail("知识库已存在");
        }
        knowledge.setUserId(userId);
        boolean save = save(knowledge);
        return save ? Result.success() : Result.fail("添加失败");
    }

    @Override
    public Result<?> delete(Long id) {
        if (id == null){
            return Result.fail("参数错误");
        }
        Long userId = UserContext.getUserId();
        Knowledge one = lambdaQuery().eq(Knowledge::getId, id).eq(Knowledge::getUserId, userId).one();
        if (one == null){
            return Result.fail("知识库不存在");
        }
        return remove(lambdaQuery().eq(Knowledge::getId, id).eq(Knowledge::getUserId, userId)) ? Result.success() : Result.fail("删除失败");
    }

    @Override
    public Page<Knowledge> listKnowLedge(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        Page<Knowledge> page = new Page<>(pageNum, pageSize);
        return lambdaQuery().eq(Knowledge::getUserId, userId).page(page);
    }
}
