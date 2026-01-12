package com.lien.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * ==============================================================================
 * Checklist Section Create/Update Request DTO
 * ==============================================================================
 * 
 * 역할:
 * - 체크리스트 섹션 생성 및 수정 시 요청 데이터를 담는 DTO
 * - 여행 준비물 관리를 위한 섹션별 체크리스트
 * - 여러 체크리스트 항목을 그룹화하여 관리
 * 
 * 이점:
 * 1. 섹션별 준비물 그룹화 (서류, 옷, 세면도구 등)
 * 2. 아이콘으로 시각적 구분
 * 3. 항목별 체크 상태 관리
 * 4. 순서 보장 (orderIndex)
 * 
 * 사용 예시:
 * <pre>
 * POST /api/templates/{templateId}/checklist-sections
 * Content-Type: application/json
 * Authorization: Bearer {JWT_TOKEN}
 * 
 * {
 *   "title": "필수 서류",
 *   "icon": "📄",
 *   "orderIndex": 1,
 *   "items": [
 *     {
 *       "label": "여권",
 *       "orderIndex": 1
 *     },
 *     {
 *       "label": "항공권 e-티켓",
 *       "orderIndex": 2
 *     },
 *     {
 *       "label": "여행자 보험증",
 *       "orderIndex": 3
 *     }
 *   ]
 * }
 * </pre>
 * 
 * 체크리스트 구조:
 * <pre>
 * Template (템플릿)
 * └── ChecklistSection (체크리스트 섹션)
 *     ├── title: "필수 서류"
 *     ├── icon: "📄"
 *     └── items: [
 *         ├── "여권" ☑
 *         ├── "항공권 e-티켓" ☐
 *         └── "여행자 보험증" ☐
 *       ]
 * 
 * ChecklistSection (체크리스트 섹션)
 *     ├── title: "의류"
 *     ├── icon: "👕"
 *     └── items: [
 *         ├── "여름 옷 3벌" ☐
 *         ├── "속옷 5벌" ☐
 *         └── "수영복" ☐
 *       ]
 * </pre>
 * 
 * 검증 규칙:
 * - title: 필수, 최대 100자
 * - icon: 선택, 최대 10자 (Emoji 권장)
 * - orderIndex: 필수, 0 이상
 * - items: 필수, 최소 1개 이상
 *   - label: 필수, 최대 200자
 *   - orderIndex: 필수, 0 이상
 * 
 * @see com.lien.entity.ChecklistSection
 * @see com.lien.entity.ChecklistItem
 * @see com.lien.entity.Template
 * @see com.lien.controller.TemplateController
 */
@Schema(description = "체크리스트 섹션 생성/수정 요청 DTO")
@Data
public class ChecklistSectionRequest {

    /**
     * 섹션 제목
     * 
     * 필수 입력 항목으로, 최대 100자까지 입력 가능합니다.
     * 체크리스트를 그룹화하는 카테고리 이름입니다.
     * 
     * 예시:
     * - "필수 서류"
     * - "의류"
     * - "세면도구"
     * - "전자기기"
     * - "상비약"
     */
    @Schema(
        description = "섹션 제목",
        example = "필수 서류",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100
    )
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100)
    private String title;

    /**
     * 아이콘
     * 
     * 선택 입력 항목으로, 최대 10자까지 입력 가능합니다.
     * 섹션을 시각적으로 구분하기 위한 아이콘입니다.
     * Emoji 사용을 권장합니다.
     * 
     * 예시:
     * - "📄" (서류)
     * - "👕" (옷)
     * - "🧴" (세면도구)
     * - "📱" (전자기기)
     * - "💊" (약)
     */
    @Schema(
        description = "아이콘 (Emoji 권장)",
        example = "📄",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 10
    )
    @Size(max = 10)
    private String icon;

    /**
     * 정렬 순서
     * 
     * 필수 입력 항목으로, 0 이상의 정수를 사용합니다.
     * 여러 섹션의 표시 순서를 결정합니다.
     * 
     * 예시:
     * - orderIndex: 1 → "필수 서류"
     * - orderIndex: 2 → "의류"
     * - orderIndex: 3 → "세면도구"
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

    /**
     * 체크리스트 항목 목록
     * 
     * 필수 입력 항목으로, 최소 1개 이상의 항목이 있어야 합니다.
     * 섹션에 속한 실제 체크리스트 항목들입니다.
     * 
     * 예시:
     * <pre>
     * [
     *   {
     *     "label": "여권",
     *     "orderIndex": 1
     *   },
     *   {
     *     "label": "항공권 e-티켓",
     *     "orderIndex": 2
     *   }
     * ]
     * </pre>
     */
    @Schema(
        description = "체크리스트 항목 목록 (최소 1개 이상)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "항목은 최소 1개 이상이어야 합니다")
    @Valid
    private List<ChecklistItemDto> items;

    /**
     * ==============================================================================
     * Checklist Item DTO (Inner Class)
     * ==============================================================================
     * 
     * 역할:
     * - 체크리스트의 개별 항목 정보
     * - 실제 준비해야 할 항목을 나타냄
     * 
     * 사용 예시:
     * <pre>
     * {
     *   "label": "여권",
     *   "orderIndex": 1
     * }
     * </pre>
     * 
     * @see com.lien.entity.ChecklistItem
     */
    @Schema(description = "체크리스트 항목 DTO")
    @Data
    public static class ChecklistItemDto {

        /**
         * 항목 내용
         * 
         * 필수 입력 항목으로, 최대 200자까지 입력 가능합니다.
         * 실제 준비해야 할 물품이나 할 일을 입력합니다.
         * 
         * 예시:
         * - "여권"
         * - "항공권 e-티켓"
         * - "여름 옷 3벌"
         * - "충전기 (휴대폰, 카메라)"
         * - "선크림 SPF 50+"
         */
        @Schema(
            description = "항목 내용",
            example = "여권",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 200
        )
        @NotBlank
        @Size(max = 200)
        private String label;

        /**
         * 정렬 순서
         * 
         * 필수 입력 항목으로, 0 이상의 정수를 사용합니다.
         * 같은 섹션 내에서 항목의 순서를 결정합니다.
         * 
         * 예시:
         * - orderIndex: 1 → "여권"
         * - orderIndex: 2 → "항공권 e-티켓"
         * - orderIndex: 3 → "여행자 보험증"
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
}

