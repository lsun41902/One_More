package com.board.one_more_project;

import com.board.one_more_project.global.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@SpringBootApplication
public class OneMoreProjectApplication {

    public static void main(String[] args) {
        // 1. 설정 클래스의 static 메서드를 깔끔하게 한 줄로 호출!
        DatabaseConfig.initDatabase(
                "jdbc:postgresql://localhost:5432/postgres",
                "postgres",
                "1234",
                "one-more-db"
        );

        // 2. 엔진 가동
        SpringApplication.run(OneMoreProjectApplication.class, args);
        System.out.println("★ 서버 시작 완료");
    }
}