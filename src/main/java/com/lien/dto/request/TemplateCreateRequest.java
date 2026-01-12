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
 * Template Create/Update Request DTO
 * ==============================================================================
 * 
 * 역할:
 * - 템플릿 생성 및 수정 시 요청 데이터를 담는 DTO
 * - 클라이언트로부터 받은 JSON 데이터를 Java 객체로 변환
 * - Bean Validation을 통한 요청 데이터 검증
 * 
 * 이점:
 * 1. Entity와 분리하여 API 계층의 독립성 유지
 * 2. 불필요한 필드 노출 방지 (id, createdAt 등)
 * 3. 유효성 검증 규칙을 명확하게 정의
 * 4. API 버전 변경 시 Entity에 영향을 주지 않음
 * 
 * 사용 예시:
 * <pre>
 * POST /api/templates
 * Content-Type: application/json
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * {
 *   "title": "제주도 3박 4일 힐링 여행",
 *   "destination": "제주도",
 *   "startDate": "2024-07-01",
 *   "endDate": "2024-07-04",
 *   "totalDays": 4,
 *   "accommodation": "신라호텔 제주",
 *   "transportation": "렌터카"
 * }
 * </pre>
 * 
 * 검증 규칙:
 * - title: 필수, 최대 200자
 * - destination: 필수, 최대 200자
 * - startDate: 필수, 날짜 형식
 * - endDate: 필수, 날짜 형식
 * - totalDays: 필수, 1 이상
 * - accommodation: 선택, 최대 200자
 * - transportation: 선택, 최대 200자
 * 
 * @see com.lien.entity.Template
 * @see com.lien.controller.TemplateController
 */
@Schema(description = "템플릿 생성/수정 요청 DTO")
@Data
public class TemplateCreateRequest {

    /**
     * 템플릿 제목
     * 
     * 필수 입력 항목으로, 최대 200자까지 입력 가능합니다.
     * 
     * 예시:
     * - "제주도 3박 4일 힐링 여행"
     * - "유럽 배낭여행 2주 계획"
     * - "일본 오사카 미식 여행"
     */
    @Schema(
        description = "템플릿 제목",
        example = "제주도 3박 4일 힐링 여행",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 200
    )
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200)
    private String title;

    /**
     * 여행 목적지
     * 
     * 필수 입력 항목으로, 최대 200자까지 입력 가능합니다.
     * 
     * 예시:
     * - "제주도"
     * - "파리, 로마, 런던"
     * - "오사카, 교토"
     */
    @Schema(
        description = "여행 목적지",
        example = "제주도",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 200
    )
    @NotBlank(message = "목적지는 필수입니다")
    @Size(max = 200)
    private String destination;

    /**
     * 여행 시작일
     * 
     * 필수 입력 항목으로, ISO 8601 날짜 형식(YYYY-MM-DD)을 사용합니다.
     * 
     * 예시:
     * - "2024-07-01"
     * - "2025-12-25"
     */
    @Schema(
        description = "여행 시작일",
        example = "2024-07-01",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "date"
    )
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    /**
     * 여행 종료일
     * 
     * 필수 입력 항목으로, ISO 8601 날짜 형식(YYYY-MM-DD)을 사용합니다.
     * 시작일보다 이후 날짜여야 합니다.
     * 
     * 예시:
     * - "2024-07-04"
     * - "2026-01-05"
     */
    @Schema(
        description = "여행 종료일",
        example = "2024-07-04",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "date"
    )
    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    /**
     * 총 여행 일수
     * 
     * 필수 입력 항목으로, 1 이상의 정수여야 합니다.
     * 시작일과 종료일을 기반으로 계산할 수 있습니다.
     * 
     * 계산 예시:
     * - startDate: 2024-07-01, endDate: 2024-07-04 → totalDays: 4
     * - startDate: 2024-07-01, endDate: 2024-07-01 → totalDays: 1
     */
    @Schema(
        description = "총 여행 일수",
        example = "4",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1"
    )
    @NotNull(message = "총 일수는 필수입니다")
    @Min(1)
    private Integer totalDays;

    /**
     * 숙소 정보
     * 
     * 선택 입력 항목으로, 최대 200자까지 입력 가능합니다.
     * 
     * 예시:
     * - "신라호텔 제주"
     * - "에어비앤비 (파리 마레 지구)"
     * - "료칸 (교토)"
     */
    @Schema(
        description = "숙소 정보",
        example = "신라호텔 제주",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 200
    )
    @Size(max = 200)
    private String accommodation;

    /**
     * 교통 수단 정보
     * 
     * 선택 입력 항목으로, 최대 200자까지 입력 가능합니다.
     * 
     * 예시:
     * - "렌터카"
     * - "기차, 비행기"
     * - "대중교통"
     */
    @Schema(
        description = "교통 수단 정보",
        example = "렌터카",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 200
    )
    @Size(max = 200)
    private String transportation;
}

