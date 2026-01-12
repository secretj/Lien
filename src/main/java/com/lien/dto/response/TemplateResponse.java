package com.lien.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * ==============================================================================
 * Template Response DTO
 * ==============================================================================
 * 
 * 역할:
 * - 템플릿 조회 시 클라이언트에게 반환하는 DTO
 * - Entity의 민감한 정보를 제외하고 필요한 정보만 전달
 * - JSON 직렬화를 통해 HTTP 응답으로 변환
 * 
 * 이점:
 * 1. Entity와 분리하여 API 계층의 독립성 유지
 * 2. 불필요한 정보 노출 방지 (비밀번호, 내부 ID 등)
 * 3. API 응답 형식 표준화
 * 4. 클라이언트 친화적인 데이터 구조
 * 
 * 사용 예시:
 * <pre>
 * GET /api/templates/1
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * 응답:
 * {
 *   "id": 1,
 *   "title": "제주도 3박 4일 힐링 여행",
 *   "destination": "제주도",
 *   "startDate": "2024-07-01",
 *   "endDate": "2024-07-04",
 *   "totalDays": 4,
 *   "accommodation": "신라호텔 제주",
 *   "transportation": "렌터카",
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-16T14:20:00"
 * }
 * </pre>
 * 
 * Entity → DTO 변환:
 * <pre>
 * Template entity = templateRepository.findById(1L).orElseThrow();
 * 
 * TemplateResponse response = TemplateResponse.builder()
 *     .id(entity.getId())
 *     .title(entity.getTitle())
 *     .destination(entity.getDestination())
 *     .startDate(entity.getStartDate())
 *     .endDate(entity.getEndDate())
 *     .totalDays(entity.getTotalDays())
 *     .accommodation(entity.getAccommodation())
 *     .transportation(entity.getTransportation())
 *     .createdAt(entity.getCreatedAt())
 *     .updatedAt(entity.getUpdatedAt())
 *     .build();
 * </pre>
 * 
 * @see com.lien.entity.Template
 * @see com.lien.controller.TemplateController
 */
@Schema(description = "템플릿 응답 DTO")
@Data
@Builder
public class TemplateResponse {

    /**
     * 템플릿 ID
     * 
     * 템플릿을 고유하게 식별하는 ID입니다.
     * 수정, 삭제 등의 작업 시 사용됩니다.
     */
    @Schema(description = "템플릿 ID", example = "1")
    private Long id;

    /**
     * 템플릿 제목
     * 
     * 예시: "제주도 3박 4일 힐링 여행"
     */
    @Schema(description = "템플릿 제목", example = "제주도 3박 4일 힐링 여행")
    private String title;

    /**
     * 여행 목적지
     * 
     * 예시: "제주도"
     */
    @Schema(description = "여행 목적지", example = "제주도")
    private String destination;

    /**
     * 여행 시작일
     * 
     * ISO 8601 날짜 형식 (YYYY-MM-DD)
     */
    @Schema(description = "여행 시작일", example = "2024-07-01", type = "string", format = "date")
    private LocalDate startDate;

    /**
     * 여행 종료일
     * 
     * ISO 8601 날짜 형식 (YYYY-MM-DD)
     */
    @Schema(description = "여행 종료일", example = "2024-07-04", type = "string", format = "date")
    private LocalDate endDate;

    /**
     * 총 여행 일수
     * 
     * 예시: 4 (3박 4일)
     */
    @Schema(description = "총 여행 일수", example = "4")
    private Integer totalDays;

    /**
     * 숙소 정보
     * 
     * 예시: "신라호텔 제주"
     */
    @Schema(description = "숙소 정보", example = "신라호텔 제주")
    private String accommodation;

    /**
     * 교통 수단 정보
     * 
     * 예시: "렌터카"
     */
    @Schema(description = "교통 수단 정보", example = "렌터카")
    private String transportation;

    /**
     * 생성 일시
     * 
     * ISO 8601 날짜/시간 형식 (YYYY-MM-DDTHH:mm:ss)
     */
    @Schema(description = "생성 일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time")
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     * 
     * ISO 8601 날짜/시간 형식 (YYYY-MM-DDTHH:mm:ss)
     */
    @Schema(description = "수정 일시", example = "2024-01-16T14:20:00", type = "string", format = "date-time")
    private LocalDateTime updatedAt;
}

