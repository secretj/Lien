package com.lien.dto.response;

import com.lien.entity.LocationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * ==============================================================================
 * Location Response DTO
 * ==============================================================================
 * 
 * 역할:
 * - 위치 정보 조회 시 클라이언트에게 반환하는 DTO
 * - Google Maps 연동을 위한 좌표 정보 포함
 * - 공개/비공개 상태 정보 포함
 * 
 * 이점:
 * 1. Entity와 분리하여 API 계층의 독립성 유지
 * 2. Google Maps API 연동에 필요한 정보 제공
 * 3. 카테고리별 필터링 지원
 * 4. 권한 관리 정보 포함 (isPublic)
 * 
 * 사용 예시:
 * <pre>
 * GET /api/locations/1
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * 응답:
 * {
 *   "id": 1,
 *   "name": "성산일출봉",
 *   "category": "ATTRACTION",
 *   "latitude": 33.4586,
 *   "longitude": 126.9409,
 *   "address": "제주특별자치도 서귀포시 성산읍 성산리 1",
 *   "description": "유네스코 세계자연유산, 일출 명소",
 *   "isPublic": true,
 *   "createdAt": "2024-01-15T10:30:00"
 * }
 * </pre>
 * 
 * Google Maps 연동 예시:
 * <pre>
 * // 1. API로 위치 정보 조회
 * LocationResponse location = getLocation(1L);
 * 
 * // 2. Google Maps에 마커 표시
 * const marker = new google.maps.Marker({
 *   position: { lat: location.latitude, lng: location.longitude },
 *   map: map,
 *   title: location.name
 * });
 * 
 * // 3. 정보 윈도우 생성
 * const infoWindow = new google.maps.InfoWindow({
 *   content: `
 *     <h3>${location.name}</h3>
 *     <p>${location.description}</p>
 *     <p>${location.address}</p>
 *   `
 * });
 * </pre>
 * 
 * Entity → DTO 변환:
 * <pre>
 * Location entity = locationRepository.findById(1L).orElseThrow();
 * 
 * LocationResponse response = LocationResponse.builder()
 *     .id(entity.getId())
 *     .name(entity.getName())
 *     .category(entity.getCategory())
 *     .latitude(entity.getLatitude())
 *     .longitude(entity.getLongitude())
 *     .address(entity.getAddress())
 *     .description(entity.getDescription())
 *     .isPublic(entity.getIsPublic())
 *     .createdAt(entity.getCreatedAt())
 *     .build();
 * </pre>
 * 
 * @see com.lien.entity.Location
 * @see com.lien.entity.LocationCategory
 * @see com.lien.controller.LocationController
 */
@Schema(description = "위치 응답 DTO")
@Data
@Builder
public class LocationResponse {

    /**
     * 위치 ID
     * 
     * 위치를 고유하게 식별하는 ID입니다.
     * 활동 추가 시 locationId로 사용됩니다.
     */
    @Schema(description = "위치 ID", example = "1")
    private Long id;

    /**
     * 위치 이름
     * 
     * 예시: "성산일출봉"
     */
    @Schema(description = "위치 이름", example = "성산일출봉")
    private String name;

    /**
     * 위치 카테고리
     * 
     * 가능한 값:
     * - ATTRACTION: 관광지
     * - ACCOMMODATION: 숙소
     * - RESTAURANT: 음식점
     * - CAFE: 카페
     * - SHOPPING: 쇼핑
     * - TRANSPORT: 교통
     */
    @Schema(
        description = "위치 카테고리",
        example = "ATTRACTION",
        allowableValues = {"ATTRACTION", "ACCOMMODATION", "RESTAURANT", "CAFE", "SHOPPING", "TRANSPORT"}
    )
    private LocationCategory category;

    /**
     * 위도 (Latitude)
     * 
     * Google Maps API에서 사용되는 좌표 정보입니다.
     * -90.0 ~ 90.0 범위의 실수 값입니다.
     * 
     * 예시: 33.4586 (성산일출봉)
     */
    @Schema(description = "위도 (Latitude)", example = "33.4586")
    private Double latitude;

    /**
     * 경도 (Longitude)
     * 
     * Google Maps API에서 사용되는 좌표 정보입니다.
     * -180.0 ~ 180.0 범위의 실수 값입니다.
     * 
     * 예시: 126.9409 (성산일출봉)
     */
    @Schema(description = "경도 (Longitude)", example = "126.9409")
    private Double longitude;

    /**
     * 주소
     * 
     * 예시: "제주특별자치도 서귀포시 성산읍 성산리 1"
     */
    @Schema(description = "주소", example = "제주특별자치도 서귀포시 성산읍 성산리 1")
    private String address;

    /**
     * 설명
     * 
     * 위치에 대한 추가 정보입니다.
     * 
     * 예시: "유네스코 세계자연유산, 일출 명소"
     */
    @Schema(description = "설명", example = "유네스코 세계자연유산, 일출 명소")
    private String description;

    /**
     * 공개 여부
     * 
     * - true: 다른 사용자도 조회 가능
     * - false: 본인만 조회 가능
     */
    @Schema(description = "공개 여부 (true: 공개, false: 비공개)", example = "true")
    private Boolean isPublic;

    /**
     * 생성 일시
     * 
     * ISO 8601 날짜/시간 형식 (YYYY-MM-DDTHH:mm:ss)
     */
    @Schema(description = "생성 일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time")
    private LocalDateTime createdAt;
}

