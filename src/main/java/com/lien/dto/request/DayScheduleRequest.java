package com.lien.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

/**
 * ==============================================================================
 * Day Schedule Create/Update Request DTO
 * ==============================================================================
 * 
 * 역할:
 * - 일별 일정 생성 및 수정 시 요청 데이터를 담는 DTO
 * - 템플릿의 각 날짜별 일정 정보 관리
 * 
 * 이점:
 * 1. 일차별 일정 구조화
 * 2. 날짜 기반 일정 관리
 * 3. 시각적 구분을 위한 색상 설정
 * 4. 일정 제목으로 빠른 파악 가능
 * 
 * 사용 예시:
 * <pre>
 * POST /api/templates/{templateId}/days
 * Content-Type: application/json
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * {
 *   "dayNumber": 1,
 *   "date": "2024-07-01",
 *   "title": "제주 동부 관광 (성산일출봉, 우도)",
 *   "color": "#FF5733"
 * }
 * </pre>
 * 
 * 데이터 구조:
 * <pre>
 * Template (템플릿)
 * └── DaySchedule (일별 일정)
 *     ├── dayNumber: 1 (첫째 날)
 *     ├── date: 2024-07-01
 *     ├── title: "제주 동부 관광"
 *     └── Activities (활동 목록)
 *         ├── 06:00 - 성산일출봉 등반
 *         ├── 09:00 - 우도 배 탑승
 *         └── 12:00 - 해녀의 집 점심
 * </pre>
 * 
 * 검증 규칙:
 * - dayNumber: 필수, 1 이상
 * - date: 필수, ISO 8601 날짜 형식
 * - title: 필수, 최대 100자
 * - color: 선택, 최대 20자 (HEX 색상 코드 권장)
 * 
 * @see com.lien.entity.DaySchedule
 * @see com.lien.entity.Template
 * @see com.lien.controller.TemplateController
 */
@Schema(description = "일별 일정 생성/수정 요청 DTO")
@Data
public class DayScheduleRequest {

    /**
     * 일차 번호
     * 
     * 필수 입력 항목으로, 1 이상의 정수를 사용합니다.
     * 여행의 몇 번째 날인지를 나타냅니다.
     * 
     * 예시:
     * - 1: 첫째 날
     * - 2: 둘째 날
     * - 3: 셋째 날
     */
    @Schema(
        description = "일차 번호 (첫째 날: 1, 둘째 날: 2, ...)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1"
    )
    @NotNull
    @Min(1)
    private Integer dayNumber;

    /**
     * 날짜
     * 
     * 필수 입력 항목으로, ISO 8601 날짜 형식(YYYY-MM-DD)을 사용합니다.
     * 템플릿의 startDate와 endDate 사이의 날짜여야 합니다.
     * 
     * 예시:
     * - "2024-07-01"
     * - "2025-12-25"
     */
    @Schema(
        description = "날짜 (ISO 8601 형식)",
        example = "2024-07-01",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "date"
    )
    @NotNull
    private LocalDate date;

    /**
     * 일정 제목
     * 
     * 필수 입력 항목으로, 최대 100자까지 입력 가능합니다.
     * 해당 날짜의 주요 활동이나 일정을 간단히 요약합니다.
     * 
     * 예시:
     * - "제주 동부 관광 (성산일출봉, 우도)"
     * - "파리 시내 투어 (에펠탑, 루브르 박물관)"
     * - "휴식 및 자유 시간"
     */
    @Schema(
        description = "일정 제목",
        example = "제주 동부 관광 (성산일출봉, 우도)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100
    )
    @NotBlank
    @Size(max = 100)
    private String title;

    /**
     * 색상 코드
     * 
     * 선택 입력 항목으로, 최대 20자까지 입력 가능합니다.
     * 캘린더나 일정 UI에서 시각적으로 구분하기 위해 사용합니다.
     * HEX 색상 코드(#RRGGBB) 형식을 권장합니다.
     * 
     * 예시:
     * - "#FF5733" (빨간색 계열)
     * - "#3498DB" (파란색 계열)
     * - "#2ECC71" (초록색 계열)
     * - "#F39C12" (주황색 계열)
     */
    @Schema(
        description = "색상 코드 (HEX 형식 권장)",
        example = "#FF5733",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 20
    )
    @Size(max = 20)
    private String color;
}

