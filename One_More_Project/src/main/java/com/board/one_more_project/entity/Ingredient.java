package com.board.one_more_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity // 이 클래스는 DB 테이블과 연결된다는 표시입니다.
@Table(name = "ingredients") // 'ingredients' 테이블과 매핑됩니다.
@Getter // Lombok: 모든 필드의 Getter 메서드 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성 (JPA 필수)
public class Ingredient {

    @Id // PK (Primary Key) 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // SERIAL (Auto Increment)
    private Long id;

    @Column(nullable = false, unique = true) // NULL 불가, 중복 불가
    private String name; // 재료 이름 (예: 돼지고기, 양파)

    @Column(length = 50)
    private String category; // 재료 카테고리 (예: 육류, 채소, 가공품)

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // 데이터가 저장되기 전에 실행되는 로직
    @PrePersist
    public void onPrePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}