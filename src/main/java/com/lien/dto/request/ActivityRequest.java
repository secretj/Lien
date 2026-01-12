package com.lien.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ==============================================================================
 * Activity Create/Update Request DTO
 * ==============================================================================
 * 
 * 역할:
 * - 활동 생성 및 수정 시 요청 데이터를 담는 DTO
 * - 일별 일정 내의 개별 활동 정보 관리
 * - 위치 간 이동 경로 정보 포함
 * 
 * 이점:
 * 1. 시간대별 활동 상세 관리
 * 2. 위치 정보와 연동 (Google Maps)
 * 3. 이동 경로 추적 (previousLocationId)
 * 4. 순서 보장 (orderIndex)
 * 
 * 사용 예시:
 * <pre>
 * POST /api/templates/{templateId}/days/{dayId}/activities
 * Content-Type: application/json
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * {
 *   "time": "06:00 - 08:00",
 *   "description": "성산일출봉 등반 및 일출 관람",
 *   "locationId": 1,
 *   "previousLocationId": null,
 *   "orderIndex": 1
 * }
 * </pre>
 * 
 * 이동 경로 관리:
 * <pre>
 * Activity 1: 호텔 체크인 (locationId: 10, previousLocationId: null)
 * Activity 2: 성산일출봉 (locationId: 1, previousLocationId: 10)  ← 호텔 → 성산일출봉
 * Activity 3: 우도 (locationId: 2, previousLocationId: 1)         ← 성산일출봉 → 우도
 * Activity 4: 해녀의 집 (locationId: 3, previousLocationId: 2)    ← 우도 → 해녀의 집
 * 
 * Google Maps Directions API를 사용하여 각 구간의 이동 시간, 거리 계산 가능:
 * - 호텔 → 성산일출봉: 차량 40분, 35km
 * - 성산일출봉 → 우도: 도보 10분 + 배 15분
 * - 우도 → 해녀의 집: 배 15분 + 차량 20분
 * </pre>
 * 
 * 검증 규칙:
 * - time: 필수, 최대 50자
 * - description: 필수, 최대 300자
 * - locationId: 필수, 위치 ID (Location 엔티티 참조)
 * - previousLocationId: 선택, 이전 위치 ID
 * - orderIndex: 필수, 0 이상 (정렬 순서)
 * 
 * @see com.lien.entity.Activity
 * @see com.lien.entity.Location
 * @see com.lien.entity.DaySchedule
 * @see com.lien.controller.TemplateController
 */
@Schema(description = "활동 생성/수정 요청 DTO")
@Data
public class ActivityRequest {

    /**
     * 시간
     * 
     * 필수 입력 항목으로, 최대 50자까지 입력 가능합니다.
     * 활동이 진행되는 시간대를 나타냅니다.
     * 
     * 형식 예시:
     * - "06:00 - 08:00" (시작 ~ 종료)
     * - "09:00" (시작 시간만)
     * - "오전" (시간대)
     * - "종일" (하루 종일)
     */
    @Schema(
        description = "활동 시간 (형식 자유)",
        example = "06:00 - 08:00",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 50
    )
    @NotBlank
    @Size(max = 50)
    private String time;

    /**
     * 활동 설명
     * 
     * 필수 입력 항목으로, 최대 300자까지 입력 가능합니다.
     * 활동의 상세 내용을 입력합니다.
     * 
     * 예시:
     * - "성산일출봉 등반 및 일출 관람. 트레킹화 필수!"
     * - "우도 자전거 투어. 땅콩 아이스크림 맛보기"
     * - "해녀의 집에서 해산물 점심 식사"
     */
    @Schema(
        description = "활동 상세 설명",
        example = "성산일출봉 등반 및 일출 관람. 트레킹화 필수!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 300
    )
    @NotBlank
    @Size(max = 300)
    private String description;

    /**
     * 위치 ID
     * 
     * 필수 입력 항목으로, Location 엔티티의 ID를 참조합니다.
     * 활동이 진행되는 장소를 나타냅니다.
     * 
     * 사용 방법:
     * 1. POST /api/locations로 위치 정보 먼저 생성
     * 2. 생성된 Location의 id를 여기에 입력
     * 
     * 예시:
     * - 1 (성산일출봉)
     * - 2 (우도)
     * - 10 (신라호텔 제주)
     */
    @Schema(
        description = "위치 ID (Location 엔티티 참조)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private Long locationId;

    /**
     * 이전 위치 ID
     * 
     * 선택 입력 항목으로, 이동 경로 추적에 사용됩니다.
     * 이전 활동의 locationId를 입력하면, 두 위치 간의 이동 정보를 관리할 수 있습니다.
     * 
     * 사용 시나리오:
     * - null: 일정의 첫 활동 (출발지)
     * - 이전 활동의 locationId: 이동 경로 추적
     * 
     * 예시:
     * <pre>
     * Activity 1: 호텔 (locationId: 10, previousLocationId: null)
     * Activity 2: 성산일출봉 (locationId: 1, previousLocationId: 10)
     * Activity 3: 우도 (locationId: 2, previousLocationId: 1)
     * 
     * → Google Maps Directions API로 경로 계산:
     *   - 호텔(10) → 성산일출봉(1): 차량 40분
     *   - 성산일출봉(1) → 우도(2): 도보 + 배 25분
     * </pre>
     */
    @Schema(
        description = "이전 위치 ID (이동 경로 추적용, 첫 활동은 null)",
        example = "10",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long previousLocationId;

    /**
     * 정렬 순서
     * 
     * 필수 입력 항목으로, 0 이상의 정수를 사용합니다.
     * 같은 일정 내에서 활동의 순서를 결정합니다.
     * 
     * 정렬 규칙:
     * - 0, 1, 2, 3, ... 순서대로 정렬
     * - 같은 orderIndex를 가진 활동은 생성 시간 순
     * 
     * 예시:
     * - orderIndex: 1 → 첫 번째 활동
     * - orderIndex: 2 → 두 번째 활동
     * - orderIndex: 3 → 세 번째 활동
     */
    @Schema(
        description = "정렬 순서 (0부터 시작)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0"
    )
    @NotNull
    @Min(0)
    private Integer orderIndex;
}

