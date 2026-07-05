package com.mj.aiknowledgebase.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.IllegalSQLInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器链（插件都加在这里）
     *
     * 常用插件：
     *   PaginationInnerInterceptor     分页查询
     *   BlockAttackInnerInterceptor    防止全表 UPDATE / DELETE
     *   OptimisticLockerInnerInterceptor  乐观锁（实体类加 @Version 字段）
     *   IllegalSQLInnerInterceptor     SQL 性能检查（拦截全表扫描等慢SQL）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ① 分页插件（必须）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);  // 单次最多查 500 条，防止一次查太多
        interceptor.addInnerInterceptor(paginationInterceptor);

        // ② 防止全表更新/删除（安全兜底，生产必加）
        // 拦截：UPDATE user SET name='x'（没写 WHERE）
        // 拦截：DELETE FROM user（没写 WHERE）
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // ③ SQL 性能规范检查（可选，开发环境很有用）
        // 拦截：没走索引的查询、全表扫描等
//        interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充 create_time / update_time
     *
     * 配合实体类上的注解使用：
     *   @TableField(fill = FieldFill.INSERT)         → insert 时自动填入
     *   @TableField(fill = FieldFill.INSERT_UPDATE)  → insert 和 update 时都自动填入
     */
    @Bean
    public MetaObjectHandler myMetaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
            }
        };
    }
}
