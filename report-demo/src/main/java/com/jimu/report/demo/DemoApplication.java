package com.jimu.report.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 演示应用入口。作为宿主示例：仅引入 report-spring-boot-starter 依赖并做 yml 配置，
 * 即可获得报表 API、设计器页面（前端构建后随 jar 提供）与元数据表。
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
