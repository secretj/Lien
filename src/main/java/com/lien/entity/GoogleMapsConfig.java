package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * ==============================================================================
 * GoogleMapsConfig Entity (Google Maps 설정 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - Google Maps API 키 관리
 * - API 키 유효성 검증 및 상태 추적
 * - 여러 API 키 관리 (할당량 분산)
 * 
 * 이점:
 * 1. API 키 중앙 관리
 * 2. 자동 유효성 검증
 * 3. 만료된 키 자동 감지
 * 4. 여러 키로 부하 분산
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE google_maps_config (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     api_key VARCHAR(500) NOT NULL,
 *     is_valid BOOLEAN NOT NULL DEFAULT FALSE,
 *     last_checked DATETIME,
 *     created_at DATETIME NOT NULL,
 *     updated_at DATETIME NOT NULL
 * );
 * </pre>
 * 
 * Google Maps API 기능:
 * 1. Maps JavaScript API: 지도 표시
 * 2. Places API: 장소 검색 및 상세 정보
 * 3. Geocoding API: 주소 → 좌표 변환
 * 4. Directions API: 경로 계산
 * 5. Distance Matrix API: 거리/시간 계산
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // API 키 등록
 * GoogleMapsConfig config = GoogleMapsConfig.builder()
 *     .apiKey("AIzaSyD1234567890abcdefghijklmnopqr")
 *     .isValid(false)  // 초기값
 *     .build();
 * googleMapsConfigRepository.save(config);
 * 
 * // API 키 검증
 * boolean isValid = validateApiKey(config.getApiKey());
 * config.setIsValid(isValid);
 * config.setLastChecked(LocalDateTime.now());
 * googleMapsConfigRepository.save(config);
 * 
 * // 유효한 API 키 조회
 * Optional<GoogleMapsConfig> validConfig = googleMapsConfigRepository
 *     .findFirstByIsValidTrueOrderByLastCheckedAsc();
 * 
 * if (validConfig.isPresent()) {
 *     String apiKey = validConfig.get().getApiKey();
 *     // Google Maps API 호출
 *     GeoApiContext context = new GeoApiContext.Builder()
 *         .apiKey(apiKey)
 *         .build();
 * }
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+------------------------------+----------+---------------------+---------------------+
 * | id | api_key                      | is_valid | last_checked        | created_at          |
 * +----+------------------------------+----------+---------------------+---------------------+
 * | 1  | AIzaSyD1234567890abc...     | TRUE     | 2024-01-15 10:30:00 | 2024-01-01 09:00:00 |
 * | 2  | AIzaSyE9876543210xyz...     | TRUE     | 2024-01-15 10:25:00 | 2024-01-05 14:00:00 |
 * | 3  | AIzaSyF5555555555old...     | FALSE    | 2024-01-10 08:00:00 | 2023-12-15 11:00:00 |
 * +----+------------------------------+----------+---------------------+---------------------+
 * </pre>
 * 
 * API 키 검증 예시:
 * <pre>
 * {@code
 * public boolean validateApiKey(String apiKey) {
 *     try {
 *         GeoApiContext context = new GeoApiContext.Builder()
 *             .apiKey(apiKey)
 *             .build();
 *         
 *         // 간단한 Geocoding 요청으로 검증
 *         GeocodingResult[] results = GeocodingApi.geocode(context, "서울")
 *             .await();
 *         
 *         return results != null && results.length > 0;
 *     } catch (Exception e) {
 *         // API 키 오류 (INVALID_REQUEST, REQUEST_DENIED 등)
 *         return false;
 *     }
 * }
 * }
 * </pre>
 * 
 * 할당량 관리:
 * - Google Maps API는 일일 요청 제한 있음
 * - 여러 API 키를 등록하여 부하 분산
 * - 로드 밸런싱: lastChecked 기준으로 가장 오래된 키 선택
 * 
 * 정기 검증:
 * <pre>
 * {@code
 * @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
 * public void validateAllApiKeys() {
 *     List<GoogleMapsConfig> configs = googleMapsConfigRepository.findAll();
 *     
 *     for (GoogleMapsConfig config : configs) {
 *         boolean isValid = validateApiKey(config.getApiKey());
 *         config.setIsValid(isValid);
 *         config.setLastChecked(LocalDateTime.now());
 *         googleMapsConfigRepository.save(config);
 *     }
 * }
 * }
 * </pre>
 * 
 * 보안 고려사항:
 * - API 키는 민감 정보이므로 암호화 저장 권장
 * - 로그에 API 키 노출 금지
 * - 환경변수 또는 Secret Manager 사용 고려
 * 
 * @see Location
 * @see Activity
 */
@Entity
@Table(name = "google_maps_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GoogleMapsConfig {
    
    /**
     * 설정 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Google Maps API 키
     * 
     * 형식: "AIzaSy" + 33자의 영숫자 (총 39자)
     * 
     * 예시:
     * - "AIzaSyD1234567890abcdefghijklmnopqrstuv"
     * - "AIzaSyE9876543210zyxwvutsrqponmlkjihgf"
     * 
     * 생성 방법:
     * 1. Google Cloud Console 접속
     * 2. 프로젝트 생성
     * 3. API 및 서비스 → 사용자 인증 정보
     * 4. API 키 만들기
     * 5. API 키 제한 설정 (IP 주소 또는 HTTP 리퍼러)
     * 
     * 제약사항:
     * - 일일 요청 제한: 무료 $200 크레딧
     * - 요청당 비용: API에 따라 다름
     *   * Maps JavaScript API: $7 / 1,000 요청
     *   * Places API: $17 / 1,000 요청
     *   * Directions API: $5 / 1,000 요청
     * 
     * 보안:
     * - 프로덕션 환경에서는 암호화 저장 권장
     * - API 키 제한 설정 (특정 IP만 허용)
     * - 환경변수로 관리 고려
     * 
     * 제약: 최대 500자
     */
    @Column(nullable = false, length = 500)
    private String apiKey;
    
    /**
     * API 키 유효성 여부
     * 
     * TRUE: 정상 작동
     * - API 호출 가능
     * - 활성 크레딧 있음
     * - 제한 설정 문제 없음
     * 
     * FALSE: 사용 불가
     * - API 키 만료
     * - 크레딧 소진
     * - 제한 설정 오류
     * - 삭제된 키
     * 
     * 검증 시점:
     * - 키 등록 시 (초기 검증)
     * - 정기 검증 (매일 또는 매주)
     * - API 호출 실패 시 (자동 재검증)
     * 
     * 기본값: false (검증 전)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isValid = false;
    
    /**
     * 마지막 검증 시간
     * 
     * 용도:
     * 1. 검증 필요 여부 판단 (7일 이상 지났으면 재검증)
     * 2. 로드 밸런싱 (가장 오래된 키 우선 사용)
     * 3. 통계 및 모니터링
     * 
     * null인 경우:
     * - 아직 검증하지 않음 (신규 키)
     * 
     * 예시:
     * {@code
     * // 7일 이상 검증하지 않은 키 조회
     * LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
     * List<GoogleMapsConfig> needsCheck = googleMapsConfigRepository
     *     .findByLastCheckedBeforeOrLastCheckedIsNull(weekAgo);
     * 
     * // 로드 밸런싱: 가장 오래된 유효한 키 선택
     * Optional<GoogleMapsConfig> leastUsed = googleMapsConfigRepository
     *     .findFirstByIsValidTrueOrderByLastCheckedAsc();
     * }
     */
    @Column
    private LocalDateTime lastChecked;
    
    /**
     * 생성 시간
     * 
     * @CreatedDate: JPA Auditing으로 자동 설정
     * 
     * 용도:
     * - 키 등록 시점 추적
     * - 오래된 키 정리
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     * 
     * @LastModifiedDate: JPA Auditing으로 자동 갱신
     * 
     * 용도:
     * - 키 정보 변경 추적
     * - 마지막 업데이트 시간 확인
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}