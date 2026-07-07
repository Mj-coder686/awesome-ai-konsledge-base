package com.mj.aiknowledgebase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mj.aiknowledgebase.mapper")
public class AiKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeBaseApplication.class, args);
    }

}
