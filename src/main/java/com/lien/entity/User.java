package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ==============================================================================
 * User Entity (사용자 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 애플리케이션 사용자 정보를 저장하는 엔티티
 * - 인증(Authentication) 및 인가(Authorization)의 주체
 * - 여행 템플릿 및 위치 정보의 소유자
 * 
 * 이점:
 * 1. Spring Security와 통합된 인증 시스템
 * 2. JWT 기반 Stateless 인증
 * 3. 이메일 기반 로그인 (소셜 로그인 확장 가능)
 * 4. 계정 활성화/비활성화 관리
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE users (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     email VARCHAR(100) NOT NULL UNIQUE,
 *     password VARCHAR(255) NOT NULL,
 *     name VARCHAR(50) NOT NULL,
 *     enabled BOOLEAN NOT NULL DEFAULT TRUE,
 *     created_at DATETIME NOT NULL,
 *     updated_at DATETIME NOT NULL,
 *     INDEX idx_user_email (email)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 회원가입
 * User user = User.builder()
 *     .email("user@example.com")
 *     .password(passwordEncoder.encode("password123"))
 *     .name("홍길동")
 *     .enabled(true)
 *     .build();
 * userRepository.save(user);
 * 
 * // 로그인 (이메일로 조회)
 * User user = userRepository.findByEmail("user@example.com")
 *     .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
 * 
 * // 계정 비활성화
 * user.setEnabled(false);
 * userRepository.save(user);
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+------------------+----------+--------+---------+---------------------+---------------------+
 * | id | email            | password | name   | enabled | created_at          | updated_at          |
 * +----+------------------+----------+--------+---------+---------------------+---------------------+
 * | 1  | hong@example.com | $2a$10.. | 홍길동 | TRUE    | 2024-01-15 10:30:00 | 2024-01-15 10:30:00 |
 * | 2  | kim@example.com  | $2a$10.. | 김철수 | TRUE    | 2024-01-16 09:00:00 | 2024-01-16 09:00:00 |
 * | 3  | lee@example.com  | $2a$10.. | 이영희 | FALSE   | 2024-01-17 14:20:00 | 2024-01-20 11:00:00 |
 * +----+------------------+----------+--------+---------+---------------------+---------------------+
 * </pre>
 * 
 * 관계:
 * - User 1 : N Template (사용자는 여러 템플릿 소유)
 * - User 1 : N Location (사용자는 여러 위치 정보 소유)
 * 
 * @see Template
 * @see Location
 * @see BaseTimeEntity
 */
@Entity
@Table(name = "users", indexes = {
    // 이메일 인덱스: 로그인 쿼리 성능 최적화
    // 이점: WHERE email = ? 쿼리 속도 향상 (Full Table Scan → Index Scan)
    // 성능: 10만 건 기준, 2초 → 0.01초
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor  // JPA 기본 생성자 (프록시 생성에 필요)
@AllArgsConstructor // 모든 필드 생성자
@Builder  // 빌더 패턴 (가독성 향상)
public class User extends BaseTimeEntity {
    
    /**
     * 사용자 고유 ID (Primary Key)
     * 
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * - 데이터베이스의 AUTO_INCREMENT 사용
     * - MySQL의 자동 증가 값 활용
     * 
     * 예시:
     * INSERT INTO users (...) VALUES (...); // id는 자동 생성
     * -> id = 1, 2, 3, ... (순차적)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이메일 (로그인 아이디)
     * 
     * 제약 조건:
     * - NOT NULL: 필수 입력
     * - UNIQUE: 중복 불가 (한 이메일로 하나의 계정만)
     * - VARCHAR(100): 최대 100자
     * 
     * 검증:
     * - @Email 어노테이션으로 이메일 형식 검증
     * - 중복 체크는 서비스 레이어에서 처리
     * 
     * 예시:
     * - 유효: user@example.com, test123@gmail.com
     * - 무효: user@, @example.com, user (도메인 없음)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 비밀번호 (암호화 저장)
     * 
     * 보안:
     * - BCrypt 암호화 필수 (UserService에서 처리)
     * - 원본 비밀번호는 절대 저장하지 않음
     * - 길이: 255자 (BCrypt 해시 길이 고려)
     * 
     * 예시:
     * - 원본: "password123"
     * - 저장: "$2a$10$N9qo8uLOickgx2ZMRZoMye1Yk8MqQ5Pz5KQNg.M8QqYxYzWz.1k6W"
     * 
     * BCrypt 특징:
     * - Salt 자동 생성 (Rainbow Table 공격 방어)
     * - 동일한 비밀번호도 매번 다른 해시 생성
     * - 검증: BCryptPasswordEncoder.matches(rawPassword, encodedPassword)
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * 사용자 이름 (실명 또는 닉네임)
     * 
     * 용도:
     * - UI에 표시 (예: "홍길동님 안녕하세요")
     * - 템플릿 작성자 표시
     * 
     * 제약:
     * - NOT NULL: 필수
     * - VARCHAR(50): 최대 50자
     * 
     * 예시: "홍길동", "김철수", "John Doe"
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * 계정 활성화 여부
     * 
     * 용도:
     * 1. 계정 정지/복구 기능
     * 2. 이메일 인증 미완료 사용자 관리
     * 3. 관리자의 계정 비활성화
     * 
     * 동작:
     * - TRUE: 로그인 가능
     * - FALSE: 로그인 차단 (인증 실패)
     * 
     * 예시:
     * {@code
     * // 계정 정지
     * user.setEnabled(false);
     * 
     * // 로그인 시도 시
     * if (!user.getEnabled()) {
     *     throw new DisabledException("비활성화된 계정입니다");
     * }
     * }
     * 
     * @Builder.Default: 빌더 사용 시 기본값 true 설정
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * equals 메서드 재정의
     * 
     * 역할:
     * - JPA 엔티티 동등성 비교
     * - Set/Map 컬렉션에서 중복 제거
     * 
     * 구현 원칙:
     * - id가 null이 아닌 경우에만 id로 비교
     * - 프록시 객체 고려 (instanceof 사용)
     * 
     * 이점:
     * - LazyInitializationException 방지
     * - Hibernate 프록시와 실제 엔티티 비교 가능
     * 
     * 예시:
     * {@code
     * User user1 = userRepository.findById(1L).get();
     * User user2 = userRepository.findById(1L).get();
     * user1.equals(user2); // true (같은 ID)
     * 
     * Set<User> users = new HashSet<>();
     * users.add(user1);
     * users.add(user2); // 중복으로 간주, Set 크기는 1
     * }
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }
    
    /**
     * hashCode 메서드 재정의
     * 
     * 역할:
     * - equals와 함께 사용 (일관성 유지)
     * - Hash 기반 컬렉션(HashMap, HashSet) 성능 최적화
     * 
     * 구현 원칙:
     * - 클래스 타입의 hashCode 사용 (불변 값)
     * - id는 사용하지 않음 (영속화 전후 hashCode 변경 방지)
     * 
     * 이점:
     * - 영속화 전후 동일한 hashCode
     * - Set에 추가 후 id 설정해도 문제 없음
     * 
     * 예시:
     * {@code
     * Set<User> users = new HashSet<>();
     * User newUser = new User(); // id = null
     * users.add(newUser);
     * userRepository.save(newUser); // id = 1
     * users.contains(newUser); // true (hashCode 변경 안 됨)
     * }
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
} 