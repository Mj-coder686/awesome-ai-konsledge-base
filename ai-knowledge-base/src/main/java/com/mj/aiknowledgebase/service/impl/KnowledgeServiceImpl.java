package com.mj.aiknowledgebase.service.impl;

import com.mj.aiknowledgebase.domain.po.Knowledge;
import com.mj.aiknowledgebase.mapper.KnowledgeMapper;
import com.mj.aiknowledgebase.service.IKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
