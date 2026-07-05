package com.mj.aiknowledgebase.service.impl;

import com.mj.aiknowledgebase.domain.po.Document;
import com.mj.aiknowledgebase.mapper.DocumentMapper;
import com.mj.aiknowledgebase.service.IDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
