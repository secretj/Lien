package com.lien.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ==============================================================================
 * JPA Auditing 설정
 * ==============================================================================
 * 
 * 역할:
 * - JPA Auditing 기능 활성화
 * - Entity의 생성 시간, 수정 시간을 자동으로 관리
 * - {@literal @}CreatedDate, {@literal @}LastModifiedDate 어노테이션 동작 활성화
 * 
 * JPA Auditing이란?
 * - Entity의 생성 및 수정 정보를 자동으로 추적하는 기능
 * - 생성 시간(createdAt), 수정 시간(updatedAt)을 자동으로 설정
 * - 생성자(createdBy), 수정자(lastModifiedBy)도 추적 가능 (현재는 미사용)
 * 
 * 이점:
 * 1. 코드 중복 제거: 모든 Entity에서 시간 설정 코드 불필요
 * 2. 일관성 유지: 모든 Entity가 동일한 방식으로 시간 정보 관리
 * 3. 자동화: 개발자가 직접 시간을 설정할 필요 없음
 * 4. 감사 추적: 데이터 변경 이력 추적 가능
 * 
 * 사용 방법:
 * <pre>
 * // 1. JpaConfig에서 {@literal @}EnableJpaAuditing 활성화 (현재 파일)
 * {@literal @}Configuration
 * {@literal @}EnableJpaAuditing
 * public class JpaConfig {
 * }
 * 
 * // 2. BaseTimeEntity 생성
 * {@literal @}Getter
 * {@literal @}MappedSuperclass
 * {@literal @}EntityListeners(AuditingEntityListener.class)
 * public abstract class BaseTimeEntity {
 *     
 *     {@literal @}CreatedDate
 *     {@literal @}Column(nullable = false, updatable = false)
 *     private LocalDateTime createdAt;
 *     
 *     {@literal @}LastModifiedDate
 *     {@literal @}Column(nullable = false)
 *     private LocalDateTime updatedAt;
 * }
 * 
 * // 3. Entity가 BaseTimeEntity 상속
 * {@literal @}Entity
 * public class User extends BaseTimeEntity {
 *     {@literal @}Id
 *     {@literal @}GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 *     
 *     private String email;
 *     private String name;
 * }
 * </pre>
 * 
 * 동작 과정:
 * <pre>
 * 1. Entity 저장 (INSERT)
 *    → {@literal @}PrePersist 콜백 실행
 *    → {@literal @}CreatedDate 필드에 현재 시간 설정
 *    → {@literal @}LastModifiedDate 필드에 현재 시간 설정
 *    
 *    예시:
 *    User user = new User("user@example.com", "password", "홍길동");
 *    userRepository.save(user);
 *    // user.getCreatedAt() = 2024-01-15T10:30:00
 *    // user.getUpdatedAt() = 2024-01-15T10:30:00
 * 
 * 2. Entity 수정 (UPDATE)
 *    → {@literal @}PreUpdate 콜백 실행
 *    → {@literal @}LastModifiedDate 필드에 현재 시간 설정
 *    → {@literal @}CreatedDate 필드는 변경되지 않음 (updatable = false)
 *    
 *    예시:
 *    user.setName("김철수");
 *    userRepository.save(user);
 *    // user.getCreatedAt() = 2024-01-15T10:30:00 (변경 없음)
 *    // user.getUpdatedAt() = 2024-01-16T14:20:00 (업데이트됨)
 * </pre>
 * 
 * 적용된 Entity:
 * - User: 사용자 정보 (생성 시간, 수정 시간)
 * - Template: 템플릿 정보 (생성 시간, 수정 시간)
 * - Location: 위치 정보 (생성 시간, 수정 시간)
 * - DaySchedule: 일별 일정 (생성 시간, 수정 시간)
 * - Activity: 활동 정보 (생성 시간, 수정 시간)
 * - ChecklistSection: 체크리스트 섹션 (생성 시간, 수정 시간)
 * - ChecklistItem: 체크리스트 항목 (생성 시간, 수정 시간)
 * - GoogleMapsConfig: Google Maps 설정 (생성 시간, 수정 시간)
 * 
 * 확장 가능한 기능:
 * <pre>
 * // 생성자, 수정자 추적 (현재는 미사용)
 * {@literal @}CreatedBy
 * {@literal @}Column(nullable = false, updatable = false)
 * private String createdBy;  // 생성자 이메일
 * 
 * {@literal @}LastModifiedBy
 * {@literal @}Column(nullable = false)
 * private String lastModifiedBy;  // 수정자 이메일
 * 
 * // AuditorAware 구현 필요
 * {@literal @}Bean
 * public AuditorAware<String> auditorProvider() {
 *     return () -> {
 *         // SecurityContext에서 현재 로그인한 사용자 이메일 추출
 *         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 *         if (authentication == null || !authentication.isAuthenticated()) {
 *             return Optional.empty();
 *         }
 *         return Optional.of(authentication.getName());
 *     };
 * }
 * </pre>
 * 
 * 주의사항:
 * 1. {@literal @}EnableJpaAuditing은 한 곳에서만 활성화
 * 2. Entity에 {@literal @}EntityListeners(AuditingEntityListener.class) 필수
 * 3. 테스트 시 Auditing이 동작하지 않을 수 있음 (테스트 설정 확인 필요)
 * 
 * @see com.lien.entity.BaseTimeEntity
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 * @see org.springframework.data.annotation.CreatedDate
 * @see org.springframework.data.annotation.LastModifiedDate
 */
@Configuration
@EnableJpaAuditing  // JPA Auditing 기능 활성화
public class JpaConfig {
    // JPA Auditing 설정만 담당하므로 별도 메서드 불필요
    // {@literal @}EnableJpaAuditing 어노테이션만으로 충분
}