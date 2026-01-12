package com.lien.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * ==============================================================================
 * Location Entity (위치 엔티티)
 * ==============================================================================
 * 
 * 역할:
 * - 여행지, 숙소, 식당 등의 위치 정보를 저장하는 엔티티
 * - Google Maps API 연동을 위한 좌표 정보 포함
 * - 공개/비공개 설정으로 위치 공유 기능
 * 
 * 이점:
 * 1. 정확한 위치 정보 (위도/경도)
 * 2. 카테고리별 분류 (관광지, 호텔, 식당 등)
 * 3. 사용자 간 위치 정보 공유
 * 4. Google Maps 연동 (지도 표시, 경로 계산)
 * 
 * 데이터베이스 스키마:
 * <pre>
 * CREATE TABLE locations (
 *     id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *     user_id BIGINT,
 *     name VARCHAR(200) NOT NULL,
 *     category VARCHAR(50) NOT NULL,
 *     latitude DOUBLE NOT NULL,
 *     longitude DOUBLE NOT NULL,
 *     address VARCHAR(500) NOT NULL,
 *     description TEXT,
 *     is_public BOOLEAN NOT NULL DEFAULT FALSE,
 *     created_at DATETIME NOT NULL,
 *     FOREIGN KEY (user_id) REFERENCES users(id)
 * );
 * </pre>
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * // 위치 생성 (공개)
 * Location seongsan = Location.builder()
 *     .user(user)
 *     .name("성산일출봉")
 *     .category(LocationCategory.ATTRACTION)
 *     .latitude(33.4584)
 *     .longitude(126.9423)
 *     .address("제주특별자치도 서귀포시 성산읍 성산리")
 *     .description("유네스코 세계자연유산, 일출 명소")
 *     .isPublic(true)  // 다른 사용자도 사용 가능
 *     .build();
 * 
 * // Google Maps 링크 생성
 * String mapsUrl = String.format(
 *     "https://www.google.com/maps?q=%f,%f",
 *     seongsan.getLatitude(),
 *     seongsan.getLongitude()
 * );
 * 
 * // 거리 계산 (Haversine Formula)
 * double distance = calculateDistance(location1, location2);
 * System.out.println(distance + "km");
 * 
 * // 카테고리별 검색
 * List<Location> restaurants = locationRepository
 *     .findByCategory(LocationCategory.RESTAURANT);
 * }
 * </pre>
 * 
 * 테이블 데이터 예시:
 * <pre>
 * +----+---------+------------------+------------+-----------+------------+----------------------------+-----------+
 * | id | user_id | name             | category   | latitude  | longitude  | address                    | is_public |
 * +----+---------+------------------+------------+-----------+------------+----------------------------+-----------+
 * | 1  | 1       | 성산일출봉       | ATTRACTION | 33.4584   | 126.9423   | 제주 서귀포시 성산읍       | TRUE      |
 * | 2  | 1       | 제주 신라호텔    | HOTEL      | 33.2514   | 126.5606   | 제주시 중문관광로72번길    | FALSE     |
 * | 3  | 1       | 올레국수         | RESTAURANT | 33.2492   | 126.4134   | 제주시 연동                | TRUE      |
 * | 4  | 2       | 부산 해운대      | ATTRACTION | 35.1585   | 129.1604   | 부산 해운대구              | TRUE      |
 * +----+---------+------------------+------------+-----------+------------+----------------------------+-----------+
 * </pre>
 * 
 * Google Maps 연동 예시:
 * <pre>
 * // 지도에 마커 표시
 * var marker = new google.maps.Marker({
 *     position: {lat: 33.4584, lng: 126.9423},
 *     map: map,
 *     title: "성산일출봉"
 * });
 * 
 * // 경로 계산
 * var directionsService = new google.maps.DirectionsService();
 * directionsService.route({
 *     origin: {lat: 33.2514, lng: 126.5606},  // 호텔
 *     destination: {lat: 33.4584, lng: 126.9423},  // 성산일출봉
 *     travelMode: 'DRIVING'
 * }, callback);
 * </pre>
 * 
 * 관계:
 * - Location N : 1 User (위치는 한 사용자가 등록, null 가능 - 시스템 기본 위치)
 * - Location 1 : N Activity (위치는 여러 활동에서 사용)
 * 
 * @see User
 * @see Activity
 * @see LocationCategory
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Location {
    
    /**
     * 위치 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 등록 사용자 (다대일 관계, 선택사항)
     * 
     * null인 경우:
     * - 시스템 기본 위치 (공항, 유명 관광지 등)
     * - 모든 사용자가 공유하는 위치
     * 
     * null이 아닌 경우:
     * - 사용자가 직접 등록한 위치
     * - isPublic 설정에 따라 공유 여부 결정
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * 위치 이름
     * 
     * 예시:
     * - "성산일출봉"
     * - "제주 신라호텔"
     * - "올레국수 (본점)"
     * - "제주국제공항"
     * 
     * 제약: 최대 200자
     */
    @Column(nullable = false, length = 200)
    private String name;
    
    /**
     * 위치 카테고리
     * 
     * @Enumerated(EnumType.STRING):
     * - ORDINAL 대신 STRING 사용 (열거형 순서 변경에 안전)
     * - DB에 "ATTRACTION", "HOTEL" 등 문자열로 저장
     * 
     * 카테고리:
     * - ATTRACTION: 관광지 (성산일출봉, 한라산)
     * - HOTEL: 숙소 (호텔, 리조트, 펜션)
     * - AIRPORT: 공항 (김포공항, 제주공항)
     * - RESTAURANT: 식당 (한식, 중식, 일식)
     * - MASSAGE: 마사지/스파
     * - SHOPPING: 쇼핑 (면세점, 시장)
     * 
     * 활용:
     * - 카테고리별 필터링
     * - 아이콘 표시 (지도 마커)
     * - 통계 (카테고리별 방문 횟수)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LocationCategory category;
    
    /**
     * 위도 (Latitude)
     * 
     * 범위: -90 ~ +90
     * - 양수: 북위
     * - 음수: 남위
     * 
     * 예시:
     * - 서울: 37.5665
     * - 제주: 33.4996
     * - 부산: 35.1796
     * 
     * 정밀도: 소수점 6자리 (약 11cm 정확도)
     * 
     * Google Maps Places API로 자동 입력 가능
     */
    @Column(nullable = false)
    private Double latitude;
    
    /**
     * 경도 (Longitude)
     * 
     * 범위: -180 ~ +180
     * - 양수: 동경
     * - 음수: 서경
     * 
     * 예시:
     * - 서울: 126.9780
     * - 제주: 126.5312
     * - 부산: 129.0756
     * 
     * 정밀도: 소수점 6자리 (약 11cm 정확도)
     */
    @Column(nullable = false)
    private Double longitude;
    
    /**
     * 주소
     * 
     * 형식: 도로명 주소 또는 지번 주소
     * 
     * 예시:
     * - "제주특별자치도 서귀포시 성산읍 성산리 104"
     * - "서울특별시 중구 세종대로 110 (서울시청)"
     * - "부산광역시 해운대구 해운대해변로 264"
     * 
     * 용도:
     * - 사용자에게 표시
     * - 주소 기반 검색
     * - Google Maps Geocoding API 입력
     * 
     * 제약: 최대 500자
     */
    @Column(nullable = false, length = 500)
    private String address;
    
    /**
     * 설명 (선택사항)
     * 
     * 용도:
     * - 위치에 대한 상세 정보
     * - 팁 및 추천 사항
     * - 영업시간, 전화번호 등
     * 
     * 예시:
     * - "유네스코 세계자연유산. 일출이 아름다움. 등반 시간 약 30분."
     * - "주차 가능. 영업시간: 09:00-18:00. 입장료: 성인 5,000원."
     * - "예약 필수. 전화: 064-123-4567."
     * 
     * 제약: TEXT 타입 (대용량)
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 공개 여부
     * 
     * TRUE (공개):
     * - 모든 사용자가 검색 및 사용 가능
     * - 위치 공유 기능
     * - 추천 위치로 표시
     * 
     * FALSE (비공개):
     * - 등록한 사용자만 사용 가능
     * - 개인적인 장소 (집, 회사 등)
     * - 검색 결과에 표시 안 됨
     * 
     * 기본값: false
     * 
     * 활용:
     * {@code
     * // 공개 위치만 조회
     * @Query("SELECT l FROM Location l WHERE l.isPublic = true OR l.user.id = :userId")
     * List<Location> findAccessibleLocations(@Param("userId") Long userId);
     * }
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = false;
    
    /**
     * 생성 시간
     * 
     * @CreatedDate: JPA Auditing으로 자동 설정
     * 
     * 용도:
     * - 최신 등록 위치 표시
     * - 위치 등록 통계
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}