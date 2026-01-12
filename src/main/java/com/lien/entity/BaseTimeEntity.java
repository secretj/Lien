package com.lien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ==============================================================================
 * Base Time Entity (공통 시간 필드 추상 클래스)
 * ==============================================================================
 * 
 * 역할:
 * - 모든 Entity의 생성/수정 시간을 자동으로 관리하는 추상 클래스
 * - JPA Auditing 기능을 통해 시간 자동 설정
 * 
 * 이점:
 * 1. 코드 중복 제거 (모든 Entity에서 동일한 필드 선언 불필요)
 * 2. 자동 시간 설정 (개발자가 직접 설정할 필요 없음)
 * 3. 일관된 시간 관리 (모든 Entity에서 동일한 방식)
 * 4. 데이터 추적 용이 (언제 생성/수정되었는지 확인)
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Entity
 * public class User extends BaseTimeEntity {
 *     private String email;
 *     // createdAt, updatedAt 필드 자동 포함
 * }
 * 
 * // 사용
 * User user = new User();
 * user.setEmail("test@example.com");
 * userRepository.save(user);
 * // -> createdAt: 2024-01-15 10:30:00 (자동 설정)
 * // -> updatedAt: 2024-01-15 10:30:00 (자동 설정)
 * 
 * user.setEmail("new@example.com");
 * userRepository.save(user);
 * // -> createdAt: 2024-01-15 10:30:00 (변경 안 됨)
 * // -> updatedAt: 2024-01-15 10:35:00 (자동 갱신)
 * }
 * </pre>
 * 
 * 데이터베이스 예시:
 * <pre>
 * users 테이블:
 * +----+------------------+---------------------+---------------------+
 * | id | email            | created_at          | updated_at          |
 * +----+------------------+---------------------+---------------------+
 * | 1  | user@example.com | 2024-01-15 10:30:00 | 2024-01-15 10:30:00 |
 * | 2  | test@example.com | 2024-01-15 11:00:00 | 2024-01-15 14:30:00 |
 * +----+------------------+---------------------+---------------------+
 * </pre>
 * 
 * 활성화 방법:
 * JpaConfig 클래스에 @EnableJpaAuditing 어노테이션 추가 필요
 * <pre>
 * {@code
 * @Configuration
 * @EnableJpaAuditing
 * public class JpaConfig { }
 * }
 * </pre>
 * 
 * @see org.springframework.data.annotation.CreatedDate
 * @see org.springframework.data.annotation.LastModifiedDate
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Getter
@MappedSuperclass  // JPA 상속 전략: 이 클래스 자체는 테이블로 생성되지 않음
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing 활성화
public abstract class BaseTimeEntity {
    
    /**
     * 생성 시간
     * 
     * 어노테이션:
     * - @CreatedDate: 엔티티 최초 저장 시 자동으로 현재 시간 설정
     * - @Column(nullable = false): NOT NULL 제약 조건
     * - @Column(updatable = false): 이후 UPDATE 쿼리에서 제외 (변경 불가)
     * 
     * 예시:
     * INSERT INTO users (..., created_at) VALUES (..., '2024-01-15 10:30:00')
     * UPDATE users SET email = 'new@example.com' WHERE id = 1
     * -> created_at은 UPDATE 쿼리에 포함되지 않음
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     * 
     * 어노테이션:
     * - @LastModifiedDate: 엔티티 저장/수정 시마다 자동으로 현재 시간으로 갱신
     * - @Column(nullable = false): NOT NULL 제약 조건
     * 
     * 동작:
     * - 최초 저장: createdAt과 동일한 시간
     * - 수정 시: 현재 시간으로 자동 갱신
     * 
     * 예시:
     * UPDATE users SET email = 'new@example.com', updated_at = '2024-01-15 14:30:00' WHERE id = 1
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
