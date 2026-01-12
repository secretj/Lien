package com.lien.dto.request;

import com.lien.entity.LocationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ==============================================================================
 * Location Create/Update Request DTO
 * ==============================================================================
 * 
 * 역할:
 * - 위치 정보 생성 및 수정 시 요청 데이터를 담는 DTO
 * - 여행지, 숙소, 음식점 등의 위치 정보 관리
 * - Google Maps API 연동을 위한 좌표 정보 포함
 * 
 * 이점:
 * 1. 위도/경도 검증을 통한 유효한 좌표 보장
 * 2. 카테고리별 위치 분류 (ATTRACTION, ACCOMMODATION, RESTAURANT 등)
 * 3. 공개/비공개 설정으로 개인 정보 보호
 * 4. Google Maps 연동 지원
 * 
 * 사용 예시:
 * <pre>
 * POST /api/locations
 * Content-Type: application/json
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * {
 *   "name": "성산일출봉",
 *   "category": "ATTRACTION",
 *   "latitude": 33.4586,
 *   "longitude": 126.9409,
 *   "address": "제주특별자치도 서귀포시 성산읍 성산리 1",
 *   "description": "유네스코 세계자연유산, 일출 명소",
 *   "isPublic": true
 * }
 * </pre>
 * 
 * Google Maps API 연동 예시:
 * <pre>
 * // 1. Google Maps Geocoding API로 주소 → 좌표 변환
 * GET https://maps.googleapis.com/maps/api/geocode/json?address=성산일출봉&key={API_KEY}
 * 
 * // 2. 좌표 정보 추출
 * {
 *   "latitude": 33.4586,
 *   "longitude": 126.9409
 * }
 * 
 * // 3. LocationRequest 생성
 * LocationRequest request = new LocationRequest();
 * request.setLatitude(33.4586);
 * request.setLongitude(126.9409);
 * </pre>
 * 
 * 검증 규칙:
 * - name: 필수, 최대 200자
 * - category: 필수, LocationCategory Enum 값
 * - latitude: 필수, -90.0 ~ 90.0
 * - longitude: 필수, -180.0 ~ 180.0
 * - address: 필수, 최대 500자
 * - description: 선택
 * - isPublic: 선택, 기본값 false
 * 
 * @see com.lien.entity.Location
 * @see com.lien.entity.LocationCategory
 * @see com.lien.controller.LocationController
 */
@Schema(description = "위치 생성/수정 요청 DTO")
@Data
public class LocationRequest {

    /**
     * 위치 이름
     * 
     * 필수 입력 항목으로, 최대 200자까지 입력 가능합니다.
     * 
     * 예시:
     * - "성산일출봉"
     * - "신라호텔 제주"
     * - "흑돼지 전문점 도연"
     */
    @Schema(
        description = "위치 이름",
        example = "성산일출봉",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 200
    )
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 200)
    private String name;

    /**
     * 위치 카테고리
     * 
     * 필수 입력 항목으로, LocationCategory Enum 값을 사용합니다.
     * 
     * 가능한 값:
     * - ATTRACTION: 관광지 (성산일출봉, 에펠탑 등)
     * - ACCOMMODATION: 숙소 (호텔, 에어비앤비 등)
     * - RESTAURANT: 음식점
     * - CAFE: 카페
     * - SHOPPING: 쇼핑 (백화점, 시장 등)
     * - TRANSPORT: 교통 (공항, 역 등)
     * 
     * 예시: "ATTRACTION"
     */
    @Schema(
        description = "위치 카테고리",
        example = "ATTRACTION",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ATTRACTION", "ACCOMMODATION", "RESTAURANT", "CAFE", "SHOPPING", "TRANSPORT"}
    )
    @NotNull(message = "카테고리는 필수입니다")
    private LocationCategory category;

    /**
     * 위도 (Latitude)
     * 
     * 필수 입력 항목으로, -90.0 ~ 90.0 범위의 실수 값을 사용합니다.
     * Google Maps API의 좌표 정보를 사용할 수 있습니다.
     * 
     * 좌표 예시:
     * - 성산일출봉: 33.4586
     * - 에펠탑: 48.8584
     * - 도쿄 스카이트리: 35.7101
     */
    @Schema(
        description = "위도 (Latitude)",
        example = "33.4586",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "-90.0",
        maximum = "90.0"
    )
    @NotNull(message = "위도는 필수입니다")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    /**
     * 경도 (Longitude)
     * 
     * 필수 입력 항목으로, -180.0 ~ 180.0 범위의 실수 값을 사용합니다.
     * Google Maps API의 좌표 정보를 사용할 수 있습니다.
     * 
     * 좌표 예시:
     * - 성산일출봉: 126.9409
     * - 에펠탑: 2.2945
     * - 도쿄 스카이트리: 139.8107
     */
    @Schema(
        description = "경도 (Longitude)",
        example = "126.9409",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "-180.0",
        maximum = "180.0"
    )
    @NotNull(message = "경도는 필수입니다")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    /**
     * 주소
     * 
     * 필수 입력 항목으로, 최대 500자까지 입력 가능합니다.
     * 
     * 예시:
     * - "제주특별자치도 서귀포시 성산읍 성산리 1"
     * - "프랑스 파리 7구 샹드마르스 공원"
     * - "일본 도쿄도 스미다구 오시아게 1-1-2"
     */
    @Schema(
        description = "주소",
        example = "제주특별자치도 서귀포시 성산읍 성산리 1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 500
    )
    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 500)
    private String address;

    /**
     * 설명
     * 
     * 선택 입력 항목으로, 위치에 대한 추가 정보를 입력할 수 있습니다.
     * 
     * 예시:
     * - "유네스코 세계자연유산, 일출 명소"
     * - "미슐랭 3스타, 예약 필수"
     * - "24시간 운영, 무료 Wi-Fi"
     */
    @Schema(
        description = "위치 설명",
        example = "유네스코 세계자연유산, 일출 명소",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

    /**
     * 공개 여부
     * 
     * 선택 입력 항목으로, 기본값은 false(비공개)입니다.
     * 
     * - true: 다른 사용자도 조회 가능
     * - false: 본인만 조회 가능 (기본값)
     * 
     * 사용 시나리오:
     * - 유명 관광지는 공개로 설정하여 다른 사용자와 공유
     * - 개인적으로 찾은 맛집은 비공개로 설정
     */
    @Schema(
        description = "공개 여부 (true: 공개, false: 비공개)",
        example = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        defaultValue = "false"
    )
    private Boolean isPublic = false;
}

