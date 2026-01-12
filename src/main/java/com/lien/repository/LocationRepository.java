package com.lien.repository;

import com.lien.entity.Location;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * Location Repository (위치 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - Location 엔티티의 데이터베이스 접근 계층
 * - 공개/비공개 위치 필터링
 * - 카테고리 및 키워드 검색
 * - Google Maps API 연동을 위한 위치 데이터 제공
 * 
 * 이점:
 * 1. 복잡한 검색 조건 처리
 * 2. 공개 위치 공유 기능
 * 3. 유연한 필터링 (카테고리, 키워드)
 * 4. 사용자 권한 기반 조회
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Service
 * public class LocationService {
 *     @Autowired
 *     private LocationRepository locationRepository;
 *     
 *     // 위치 검색 (공개 + 내 위치)
 *     public List<Location> searchLocations(User user, String category, String keyword) {
 *         LocationCategory cat = category != null ? 
 *             LocationCategory.valueOf(category) : null;
 *         return locationRepository.findByUserOrPublicWithFilters(user, cat, keyword);
 *     }
 *     
 *     // 내 위치 목록
 *     public List<Location> getMyLocations(User user) {
 *         return locationRepository.findByUser(user);
 *     }
 * }
 * }
 * </pre>
 * 
 * @see Location
 * @see JpaRepository
 */
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    /**
     * 접근 가능한 위치 검색 (공개 + 내 위치, 필터링)
     * 
     * 역할:
     * - 공개 위치 또는 사용자가 등록한 위치 조회
     * - 카테고리 필터링 (선택사항)
     * - 키워드 검색 (선택사항)
     * 
     * 검색 로직:
     * 1. 공개 위치 (isPublic = true) 또는 내 위치 (user = :user)
     * 2. 카테고리 필터 (null이면 전체)
     * 3. 키워드 검색 (name에 포함, 대소문자 무시)
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM locations
     * WHERE (is_public = TRUE OR user_id = ?)
     * AND (? IS NULL OR category = ?)
     * AND (? IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', ?, '%')));
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 모든 공개 위치 + 내 위치
     * List<Location> all = locationRepository
     *     .findByUserOrPublicWithFilters(user, null, null);
     * 
     * // 호텔만 검색
     * List<Location> hotels = locationRepository
     *     .findByUserOrPublicWithFilters(user, LocationCategory.HOTEL, null);
     * 
     * // "제주" 포함 검색
     * List<Location> jeju = locationRepository
     *     .findByUserOrPublicWithFilters(user, null, "제주");
     * 
     * // 제주의 관광지만 검색
     * List<Location> jejuAttractions = locationRepository
     *     .findByUserOrPublicWithFilters(user, LocationCategory.ATTRACTION, "제주");
     * 
     * // 결과 예시:
     * // - 성산일출봉 (공개, ATTRACTION)
     * // - 제주 신라호텔 (비공개, 내 위치)
     * // - 제주국제공항 (공개, AIRPORT) <- 카테고리 불일치로 제외
     * }
     * </pre>
     * 
     * 검색 결과 예시:
     * <pre>
     * // findByUserOrPublicWithFilters(user, HOTEL, "제주")
     * +----+------------------+---------+----------+
     * | id | name             | category| is_public|
     * +----+------------------+---------+----------+
     * | 1  | 제주 신라호텔    | HOTEL   | FALSE    | <- 내 위치
     * | 2  | 제주 롯데호텔    | HOTEL   | TRUE     | <- 공개
     * | 3  | 제주 파라다이스  | HOTEL   | TRUE     | <- 공개
     * +----+------------------+---------+----------+
     * </pre>
     * 
     * @param user 현재 사용자 (내 위치 포함)
     * @param category 카테고리 필터 (null이면 전체)
     * @param keyword 검색 키워드 (null이면 검색 안 함)
     * @return List<Location> 조건에 맞는 위치 목록
     */
    @Query("SELECT l FROM Location l WHERE " +
           "(l.isPublic = true OR l.user = :user) " +
           "AND (:category IS NULL OR l.category = :category) " +
           "AND (:keyword IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Location> findByUserOrPublicWithFilters(
        @Param("user") User user,
        @Param("category") LocationCategory category,
        @Param("keyword") String keyword
    );
    
    /**
     * 사용자 및 ID로 위치 조회
     * 
     * 역할:
     * - 위치 소유자 확인
     * - 수정/삭제 권한 검증
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM locations WHERE id = ? AND user_id = ?;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 위치 수정 시 소유자 확인
     * Optional<Location> location = locationRepository.findByIdAndUser(locationId, user);
     * if (!location.isPresent()) {
     *     throw new AccessDeniedException("위치가 없거나 수정 권한이 없습니다");
     * }
     * 
     * location.get().setName("새로운 이름");
     * locationRepository.save(location.get());
     * }
     * </pre>
     * 
     * @param id 위치 ID
     * @param user 사용자
     * @return Optional<Location> 위치 (없으면 Optional.empty())
     */
    Optional<Location> findByIdAndUser(Long id, User user);
    
    /**
     * 사용자의 모든 위치 조회
     * 
     * 역할:
     * - 내가 등록한 위치 목록
     * - 비공개 위치 포함
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM locations WHERE user_id = ? ORDER BY created_at DESC;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 내 위치 목록 페이지
     * List<Location> myLocations = locationRepository.findByUser(user);
     * 
     * // 공개/비공개 개수 확인
     * long publicCount = myLocations.stream()
     *     .filter(Location::getIsPublic)
     *     .count();
     * long privateCount = myLocations.size() - publicCount;
     * 
     * System.out.println("공개: " + publicCount + ", 비공개: " + privateCount);
     * }
     * </pre>
     * 
     * @param user 사용자
     * @return List<Location> 사용자의 모든 위치
     */
    List<Location> findByUser(User user);
    
    /**
     * 카테고리별 공개 위치 조회
     * 
     * 역할:
     * - 카테고리별 추천 위치
     * - 인기 위치 표시
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM locations 
     * WHERE is_public = TRUE AND category = ?
     * ORDER BY created_at DESC;
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 추천 관광지 목록
     * List<Location> attractions = locationRepository
     *     .findByIsPublicTrueAndCategory(LocationCategory.ATTRACTION);
     * 
     * // 카테고리별 개수 통계
     * for (LocationCategory category : LocationCategory.values()) {
     *     long count = locationRepository
     *         .countByIsPublicTrueAndCategory(category);
     *     System.out.println(category + ": " + count + "개");
     * }
     * }
     * </pre>
     * 
     * @param category 카테고리
     * @return List<Location> 공개 위치 목록
     */
    List<Location> findByIsPublicTrueAndCategory(LocationCategory category);
    
    /**
     * 좌표 범위 내 위치 검색
     * 
     * 역할:
     * - 지도 영역 내 위치 표시
     * - 근처 위치 검색
     * 
     * 실행 SQL:
     * <pre>
     * SELECT * FROM locations
     * WHERE latitude BETWEEN ? AND ?
     * AND longitude BETWEEN ? AND ?
     * AND (is_public = TRUE OR user_id = ?);
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 제주도 영역 (대략적인 범위)
     * double minLat = 33.2;
     * double maxLat = 33.6;
     * double minLng = 126.2;
     * double maxLng = 126.9;
     * 
     * List<Location> locationsInJeju = locationRepository
     *     .findByLatitudeBetweenAndLongitudeBetweenAndIsPublicTrueOrUser(
     *         minLat, maxLat, minLng, maxLng, user
     *     );
     * 
     * // Google Maps에 마커 표시
     * for (Location loc : locationsInJeju) {
     *     addMarker(loc.getLatitude(), loc.getLongitude(), loc.getName());
     * }
     * }
     * </pre>
     * 
     * @param minLat 최소 위도
     * @param maxLat 최대 위도
     * @param minLng 최소 경도
     * @param maxLng 최대 경도
     * @param user 현재 사용자
     * @return List<Location> 범위 내 위치 목록
     */
    @Query("SELECT l FROM Location l WHERE " +
           "l.latitude BETWEEN :minLat AND :maxLat " +
           "AND l.longitude BETWEEN :minLng AND :maxLng " +
           "AND (l.isPublic = true OR l.user = :user)")
    List<Location> findByLatitudeBetweenAndLongitudeBetweenAndIsPublicTrueOrUser(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng,
        @Param("user") User user
    );
}