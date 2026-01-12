package com.lien.repository;

import com.lien.entity.GoogleMapsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * GoogleMapsConfig Repository (Google Maps 설정 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - GoogleMapsConfig 엔티티의 데이터베이스 접근 계층
 * - API 키 관리 및 조회
 * - 유효한 API 키 로드 밸런싱
 * 
 * 이점:
 * 1. 여러 API 키 관리
 * 2. 유효한 키 자동 선택
 * 3. 로드 밸런싱 (가장 오래된 키 우선)
 * 4. API 키 검증 상태 추적
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Service
 * public class GoogleMapsService {
 *     @Autowired
 *     private GoogleMapsConfigRepository configRepository;
 *     
 *     // 유효한 API 키 가져오기 (로드 밸런싱)
 *     public String getValidApiKey() {
 *         GoogleMapsConfig config = configRepository
 *             .findFirstByIsValidTrueOrderByLastCheckedAsc()
 *             .orElseThrow(() -> new RuntimeException("유효한 API 키가 없습니다"));
 *         
 *         // 사용 시간 업데이트
 *         config.setLastChecked(LocalDateTime.now());
 *         configRepository.save(config);
 *         
 *         return config.getApiKey();
 *     }
 *     
 *     // API 키 검증
 *     @Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
 *     public void validateApiKeys() {
 *         List<GoogleMapsConfig> configs = configRepository.findAll();
 *         for (GoogleMapsConfig config : configs) {
 *             boolean isValid = checkApiKey(config.getApiKey());
 *             config.setIsValid(isValid);
 *             config.setLastChecked(LocalDateTime.now());
 *             configRepository.save(config);
 *         }
 *     }
 * }
 * }
 * </pre>
 * 
 * @see GoogleMapsConfig
 * @see JpaRepository
 */
public interface GoogleMapsConfigRepository extends JpaRepository<GoogleMapsConfig, Long> {
    
    /**
     * 최신 등록된 설정 조회
     * 
     * 역할:
     * - 가장 최근에 등록된 API 키 조회
     * - 기본 API 키 가져오기
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM google_maps_config 
     * ORDER BY id DESC 
     * LIMIT 1;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 현재 사용 중인 API 키
     * GoogleMapsConfig config = googleMapsConfigRepository
     *     .findFirstByOrderByIdDesc()
     *     .orElseThrow(() -> new RuntimeException("설정된 API 키가 없습니다"));
     * 
     * String apiKey = config.getApiKey();
     * }
     * </pre>
     * 
     * @return Optional<GoogleMapsConfig> 최신 설정
     */
    Optional<GoogleMapsConfig> findFirstByOrderByIdDesc();
    
    /**
     * 유효한 API 키 조회 (로드 밸런싱)
     * 
     * 역할:
     * - 유효한 API 키 중 가장 오래 사용하지 않은 키 선택
     * - 로드 밸런싱으로 API 할당량 분산
     * - API 키 교대 사용
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM google_maps_config 
     * WHERE is_valid = TRUE 
     * ORDER BY last_checked ASC 
     * LIMIT 1;
     * </pre>
     * 
     * 로드 밸런싱 원리:
     * 1. 유효한 키 중 lastChecked가 가장 오래된 키 선택
     * 2. 사용 후 lastChecked 업데이트
     * 3. 다음 요청 시 다른 키가 선택됨
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // API 호출 시마다 다른 키 사용 (로드 밸런싱)
     * GoogleMapsConfig config = googleMapsConfigRepository
     *     .findFirstByIsValidTrueOrderByLastCheckedAsc()
     *     .orElseThrow(() -> new RuntimeException("유효한 API 키가 없습니다"));
     * 
     * // Google Maps API 호출
     * GeoApiContext context = new GeoApiContext.Builder()
     *     .apiKey(config.getApiKey())
     *     .build();
     * 
     * // 사용 시간 업데이트 (다음 번엔 다른 키 사용)
     * config.setLastChecked(LocalDateTime.now());
     * googleMapsConfigRepository.save(config);
     * 
     * // 할당량 분산 효과:
     * // - 키 A: 10시 사용 -> lastChecked = 10:00
     * // - 키 B: 09시 사용 -> lastChecked = 09:00
     * // 다음 요청 시 키 B 선택 (lastChecked가 더 오래됨)
     * }
     * </pre>
     * 
     * @return Optional<GoogleMapsConfig> 유효한 API 키 설정
     */
    Optional<GoogleMapsConfig> findFirstByIsValidTrueOrderByLastCheckedAsc();
    
    /**
     * 모든 유효한 API 키 조회
     * 
     * 역할:
     * - 유효한 API 키 목록
     * - 통계 및 모니터링
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM google_maps_config WHERE is_valid = TRUE;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * List<GoogleMapsConfig> validConfigs = googleMapsConfigRepository
     *     .findByIsValidTrue();
     * 
     * System.out.println("유효한 API 키: " + validConfigs.size() + "개");
     * }
     * </pre>
     * 
     * @return List<GoogleMapsConfig> 유효한 API 키 목록
     */
    List<GoogleMapsConfig> findByIsValidTrue();
    
    /**
     * 검증 필요한 API 키 조회
     * 
     * 역할:
     * - 오래된 검증 데이터 갱신
     * - 정기 검증 대상 선택
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM google_maps_config 
     * WHERE last_checked < ? OR last_checked IS NULL;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 7일 이상 검증하지 않은 키
     * LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
     * List<GoogleMapsConfig> needsCheck = googleMapsConfigRepository
     *     .findByLastCheckedBeforeOrLastCheckedIsNull(weekAgo);
     * 
     * System.out.println("검증 필요: " + needsCheck.size() + "개");
     * 
     * // 재검증
     * for (GoogleMapsConfig config : needsCheck) {
     *     boolean isValid = validateApiKey(config.getApiKey());
     *     config.setIsValid(isValid);
     *     config.setLastChecked(LocalDateTime.now());
     *     googleMapsConfigRepository.save(config);
     * }
     * }
     * </pre>
     * 
     * @param dateTime 기준 시간
     * @return List<GoogleMapsConfig> 검증 필요한 설정 목록
     */
    List<GoogleMapsConfig> findByLastCheckedBeforeOrLastCheckedIsNull(LocalDateTime dateTime);
}
