package com.lien.controller;

import com.lien.dto.request.ActivityRequest;
import com.lien.dto.request.ChecklistSectionRequest;
import com.lien.dto.request.DayScheduleRequest;
import com.lien.dto.request.TemplateCreateRequest;
import com.lien.dto.response.TemplateResponse;
import com.lien.entity.User;
import com.lien.security.CurrentUser;
import com.lien.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==============================================================================
 * Template Controller (여행 템플릿 컨트롤러)
 * ==============================================================================
 * 
 * 역할:
 * - 여행 템플릿 관련 REST API 엔드포인트 제공
 * - 템플릿, 체크리스트, 일정, 활동의 CRUD 작업 처리
 * - 계층 구조 데이터 관리 (Template → Checklist/DaySchedule → Activity)
 * 
 * Base URL: /api/templates
 * 인증: JWT 토큰 필요 (Authorization: Bearer {token})
 * 
 * API 목록:
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ 1. 템플릿 관리 (Template Management)                              │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ POST   /api/templates                    : 템플릿 생성            │
 * │ GET    /api/templates                    : 템플릿 목록 조회        │
 * │ GET    /api/templates/{templateId}       : 템플릿 상세 조회        │
 * │ PUT    /api/templates/{templateId}       : 템플릿 수정            │
 * │ DELETE /api/templates/{templateId}       : 템플릿 삭제            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ 2. 체크리스트 관리 (Checklist Management)                         │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ POST   /api/templates/{templateId}/checklist-sections            │
 * │        : 체크리스트 섹션 추가                                       │
 * │ PUT    /api/templates/{templateId}/checklist-sections/{sectionId}│
 * │        : 체크리스트 섹션 수정                                       │
 * │ DELETE /api/templates/{templateId}/checklist-sections/{sectionId}│
 * │        : 체크리스트 섹션 삭제                                       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ 3. 일정 관리 (Day Schedule Management)                           │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ POST   /api/templates/{templateId}/days                          │
 * │        : 일별 일정 추가                                            │
 * │ PUT    /api/templates/{templateId}/days/{dayId}                  │
 * │        : 일별 일정 수정                                            │
 * │ DELETE /api/templates/{templateId}/days/{dayId}                  │
 * │        : 일별 일정 삭제                                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ 4. 활동 관리 (Activity Management)                               │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ POST   /api/templates/{templateId}/days/{dayId}/activities       │
 * │        : 활동 추가                                                │
 * │ PUT    /api/templates/{templateId}/days/{dayId}/activities/{activityId}│
 * │        : 활동 수정                                                │
 * │ DELETE /api/templates/{templateId}/days/{dayId}/activities/{activityId}│
 * │        : 활동 삭제                                                │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * 데이터 구조:
 * <pre>
 * Template (템플릿)
 * ├── ChecklistSection (체크리스트 섹션)
 * │   └── ChecklistItem (체크리스트 항목)
 * └── DaySchedule (일별 일정)
 *     └── Activity (활동)
 *         ├── Location (위치)
 *         └── PreviousLocation (이전 위치)
 * </pre>
 * 
 * 권한:
 * - {@code @CurrentUser} 어노테이션을 통해 현재 로그인한 사용자 자동 주입
 * - 모든 API는 사용자 본인의 데이터만 접근 가능 (소유자 검증)
 * - 다른 사용자의 데이터 접근 시 403 Forbidden 에러 발생
 * 
 * 페이징:
 * - 템플릿 목록 조회 시 Spring Data JPA의 Pageable 지원
 * - 쿼리 파라미터: page, size, sort
 * - 예: /api/templates?page=0&size=10&sort=createdAt,desc
 * 
 * 검증:
 * - {@code @Valid} 어노테이션을 통한 요청 데이터 검증
 * - DTO의 @NotNull, @NotBlank 등의 제약 조건 자동 검증
 * - 검증 실패 시 400 Bad Request 에러 발생
 * 
 * 상태 코드:
 * - 200 OK: 조회, 수정 성공
 * - 201 Created: 생성 성공
 * - 204 No Content: 삭제 성공
 * - 400 Bad Request: 잘못된 요청 (검증 실패)
 * - 403 Forbidden: 권한 없음 (다른 사용자의 데이터 접근)
 * - 404 Not Found: 리소스 없음
 * 
 * @see TemplateService
 * @see TemplateCreateRequest
 * @see TemplateResponse
 */
@Tag(name = "2. 템플릿", description = "여행 템플릿 CRUD API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 템플릿 생성
     * 
     * HTTP Method: POST
     * URL: /api/templates
     * 인증: 필요 (JWT)
     * 
     * 요청 예시:
     * <pre>
     * POST /api/templates
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
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
     * 성공 응답 (201 Created):
     * <pre>
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
     *   "updatedAt": "2024-01-15T10:30:00"
     * }
     * </pre>
     */
    @Operation(
        summary = "템플릿 생성",
        description = "새로운 여행 템플릿을 생성합니다. JWT 인증 필요"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "템플릿 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)")
    })
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.createTemplate(user, request));
    }

    /**
     * 템플릿 목록 조회 (페이징)
     * 
     * HTTP Method: GET
     * URL: /api/templates?page=0&size=10&sort=createdAt,desc
     * 인증: 필요 (JWT)
     * 
     * 쿼리 파라미터:
     * - page: 페이지 번호 (0부터 시작, 기본값: 0)
     * - size: 페이지 크기 (기본값: 20)
     * - sort: 정렬 기준 (예: createdAt,desc)
     * 
     * 요청 예시:
     * <pre>
     * GET /api/templates?page=0&size=10&sort=createdAt,desc
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (200 OK):
     * <pre>
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "title": "제주도 3박 4일 힐링 여행",
     *       "destination": "제주도",
     *       "startDate": "2024-07-01",
     *       "endDate": "2024-07-04",
     *       "totalDays": 4,
     *       "createdAt": "2024-01-15T10:30:00"
     *     },
     *     ...
     *   ],
     *   "pageable": {
     *     "pageNumber": 0,
     *     "pageSize": 10,
     *     "sort": {
     *       "sorted": true,
     *       "orders": [{"property": "createdAt", "direction": "DESC"}]
     *     }
     *   },
     *   "totalElements": 25,
     *   "totalPages": 3,
     *   "last": false,
     *   "first": true
     * }
     * </pre>
     */
    @Operation(
        summary = "템플릿 목록 조회 (페이징)",
        description = "현재 로그인한 사용자의 템플릿 목록을 페이징하여 조회합니다"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<Page<TemplateResponse>> getTemplates(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "페이징 정보 (page, size, sort)", example = "page=0&size=10&sort=createdAt,desc")
            Pageable pageable) {
        return ResponseEntity.ok(templateService.getTemplates(user, pageable));
    }

    /**
     * 템플릿 상세 조회
     * 
     * HTTP Method: GET
     * URL: /api/templates/{templateId}
     * 인증: 필요 (JWT)
     * 
     * 역할:
     * - 템플릿 기본 정보 조회
     * - 체크리스트 섹션 및 항목 조회
     * - 일별 일정 및 활동 조회
     * - Fetch Join을 통한 N+1 문제 해결
     * 
     * 요청 예시:
     * <pre>
     * GET /api/templates/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (200 OK):
     * <pre>
     * {
     *   "id": 1,
     *   "title": "제주도 3박 4일 힐링 여행",
     *   "destination": "제주도",
     *   "startDate": "2024-07-01",
     *   "endDate": "2024-07-04",
     *   "totalDays": 4,
     *   "checklistSections": [
     *     {
     *       "id": 1,
     *       "title": "준비물",
     *       "items": [
     *         {"id": 1, "label": "여권", "checked": true},
     *         {"id": 2, "label": "선크림", "checked": false}
     *       ]
     *     }
     *   ],
     *   "daySchedules": [
     *     {
     *       "id": 1,
     *       "dayNumber": 1,
     *       "date": "2024-07-01",
     *       "activities": [
     *         {
     *           "id": 1,
     *           "title": "성산일출봉 등반",
     *           "startTime": "06:00",
     *           "endTime": "08:00",
     *           "location": {
     *             "id": 1,
     *             "name": "성산일출봉",
     *             "address": "제주 서귀포시 성산읍"
     *           }
     *         }
     *       ]
     *     }
     *   ]
     * }
     * </pre>
     */
    @Operation(summary = "템플릿 상세 조회", description = "템플릿의 상세 정보를 조회합니다 (체크리스트, 일정 포함)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 템플릿)"),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<?> getTemplate(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID", example = "1") @PathVariable Long templateId) {
        return ResponseEntity.ok(templateService.getTemplateDetail(user, templateId));
    }

    /**
     * 템플릿 수정
     * 
     * HTTP Method: PUT
     * URL: /api/templates/{templateId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 요청 예시:
     * <pre>
     * PUT /api/templates/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "title": "제주도 4박 5일 힐링 여행 (수정)",
     *   "destination": "제주도",
     *   "startDate": "2024-07-01",
     *   "endDate": "2024-07-05",
     *   "totalDays": 5,
     *   "accommodation": "롯데호텔 제주",
     *   "transportation": "렌터카"
     * }
     * </pre>
     * 
     * 성공 응답 (200 OK):
     * <pre>
     * {
     *   "id": 1,
     *   "title": "제주도 4박 5일 힐링 여행 (수정)",
     *   "destination": "제주도",
     *   ...
     *   "updatedAt": "2024-01-15T11:00:00"
     * }
     * </pre>
     */
    @Operation(summary = "템플릿 수정", description = "템플릿 정보를 수정합니다 (소유자만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @PutMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Valid @RequestBody TemplateCreateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(user, templateId, request));
    }

    /**
     * 템플릿 삭제
     * 
     * HTTP Method: DELETE
     * URL: /api/templates/{templateId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 템플릿 삭제
     * - 연관된 체크리스트 섹션/항목 자동 삭제 (Cascade)
     * - 연관된 일별 일정/활동 자동 삭제 (Cascade)
     * 
     * 요청 예시:
     * <pre>
     * DELETE /api/templates/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (204 No Content):
     * (응답 본문 없음)
     */
    @Operation(summary = "템플릿 삭제", description = "템플릿 및 연관 데이터를 모두 삭제합니다 (소유자만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음")
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId) {
        templateService.deleteTemplate(user, templateId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 체크리스트 섹션 추가
     * 
     * HTTP Method: POST
     * URL: /api/templates/{templateId}/checklist-sections
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 템플릿에 새로운 체크리스트 섹션 추가
     * - 여러 체크리스트 항목 동시 추가 가능
     * 
     * 요청 예시:
     * <pre>
     * POST /api/templates/1/checklist-sections
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "title": "준비물",
     *   "orderIndex": 1,
     *   "items": [
     *     {"label": "여권", "checked": false, "orderIndex": 1},
     *     {"label": "선크림", "checked": false, "orderIndex": 2},
     *     {"label": "수영복", "checked": false, "orderIndex": 3}
     *   ]
     * }
     * </pre>
     * 
     * 성공 응답 (201 Created):
     * <pre>
     * {
     *   "id": 1,
     *   "title": "준비물",
     *   "orderIndex": 1,
     *   "items": [
     *     {"id": 1, "label": "여권", "checked": false, "orderIndex": 1},
     *     {"id": 2, "label": "선크림", "checked": false, "orderIndex": 2},
     *     {"id": 3, "label": "수영복", "checked": false, "orderIndex": 3}
     *   ]
     * }
     * </pre>
     */
    @Operation(summary = "체크리스트 섹션 추가", description = "템플릿에 체크리스트 섹션을 추가합니다")
    @PostMapping("/{templateId}/checklist-sections")
    public ResponseEntity<?> addChecklistSection(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Valid @RequestBody ChecklistSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addChecklistSection(user, templateId, request));
    }

    /**
     * 체크리스트 섹션 수정
     * 
     * HTTP Method: PUT
     * URL: /api/templates/{templateId}/checklist-sections/{sectionId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 체크리스트 섹션 제목 및 순서 변경
     * - 체크리스트 항목 추가/수정/삭제
     * 
     * 요청 예시:
     * <pre>
     * PUT /api/templates/1/checklist-sections/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "title": "필수 준비물 (수정)",
     *   "orderIndex": 1,
     *   "items": [
     *     {"id": 1, "label": "여권", "checked": true, "orderIndex": 1},
     *     {"id": 2, "label": "선크림", "checked": false, "orderIndex": 2},
     *     {"label": "모자", "checked": false, "orderIndex": 3}
     *   ]
     * }
     * </pre>
     */
    @Operation(summary = "체크리스트 섹션 수정")
    @PutMapping("/{templateId}/checklist-sections/{sectionId}")
    public ResponseEntity<?> updateChecklistSection(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "섹션 ID") @PathVariable Long sectionId,
            @Valid @RequestBody ChecklistSectionRequest request) {
        return ResponseEntity.ok(
                templateService.updateChecklistSection(user, templateId, sectionId, request));
    }

    /**
     * 체크리스트 섹션 삭제
     * 
     * HTTP Method: DELETE
     * URL: /api/templates/{templateId}/checklist-sections/{sectionId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 체크리스트 섹션 삭제
     * - 연관된 모든 체크리스트 항목 자동 삭제 (Cascade)
     * 
     * 요청 예시:
     * <pre>
     * DELETE /api/templates/1/checklist-sections/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (204 No Content):
     * (응답 본문 없음)
     */
    @Operation(summary = "체크리스트 섹션 삭제")
    @DeleteMapping("/{templateId}/checklist-sections/{sectionId}")
    public ResponseEntity<Void> deleteChecklistSection(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "섹션 ID") @PathVariable Long sectionId) {
        templateService.deleteChecklistSection(user, templateId, sectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 일별 일정 추가
     * 
     * HTTP Method: POST
     * URL: /api/templates/{templateId}/days
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 템플릿에 새로운 일별 일정 추가
     * - 날짜 및 일차 정보 설정
     * 
     * 요청 예시:
     * <pre>
     * POST /api/templates/1/days
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "dayNumber": 1,
     *   "date": "2024-07-01",
     *   "title": "제주 동부 관광"
     * }
     * </pre>
     * 
     * 성공 응답 (201 Created):
     * <pre>
     * {
     *   "id": 1,
     *   "dayNumber": 1,
     *   "date": "2024-07-01",
     *   "title": "제주 동부 관광",
     *   "activities": []
     * }
     * </pre>
     */
    @Operation(summary = "일별 일정 추가", description = "템플릿에 일별 일정을 추가합니다")
    @PostMapping("/{templateId}/days")
    public ResponseEntity<?> addDaySchedule(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Valid @RequestBody DayScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addDaySchedule(user, templateId, request));
    }

    /**
     * 일별 일정 수정
     * 
     * HTTP Method: PUT
     * URL: /api/templates/{templateId}/days/{dayId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 일별 일정의 날짜, 제목 수정
     * - 일차 번호 변경
     * 
     * 요청 예시:
     * <pre>
     * PUT /api/templates/1/days/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "dayNumber": 1,
     *   "date": "2024-07-01",
     *   "title": "제주 동부 관광 (성산일출봉, 우도)"
     * }
     * </pre>
     */
    @Operation(summary = "일별 일정 수정")
    @PutMapping("/{templateId}/days/{dayId}")
    public ResponseEntity<?> updateDaySchedule(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "일정 ID") @PathVariable Long dayId,
            @Valid @RequestBody DayScheduleRequest request) {
        return ResponseEntity.ok(
                templateService.updateDaySchedule(user, templateId, dayId, request));
    }

    /**
     * 일별 일정 삭제
     * 
     * HTTP Method: DELETE
     * URL: /api/templates/{templateId}/days/{dayId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 일별 일정 삭제
     * - 연관된 모든 활동 자동 삭제 (Cascade)
     * 
     * 요청 예시:
     * <pre>
     * DELETE /api/templates/1/days/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (204 No Content):
     * (응답 본문 없음)
     */
    @Operation(summary = "일별 일정 삭제")
    @DeleteMapping("/{templateId}/days/{dayId}")
    public ResponseEntity<Void> deleteDaySchedule(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "일정 ID") @PathVariable Long dayId) {
        templateService.deleteDaySchedule(user, templateId, dayId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 활동 추가
     * 
     * HTTP Method: POST
     * URL: /api/templates/{templateId}/days/{dayId}/activities
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 일별 일정에 새로운 활동 추가
     * - 위치 정보 연결
     * - 이동 경로 관리 (이전 위치와 현재 위치)
     * 
     * 요청 예시:
     * <pre>
     * POST /api/templates/1/days/1/activities
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "title": "성산일출봉 등반",
     *   "description": "일출 관람 및 등반",
     *   "startTime": "06:00",
     *   "endTime": "08:00",
     *   "locationId": 1,
     *   "previousLocationId": null,
     *   "orderIndex": 1,
     *   "transportMethod": "도보",
     *   "estimatedCost": 5000
     * }
     * </pre>
     * 
     * 성공 응답 (201 Created):
     * <pre>
     * {
     *   "id": 1,
     *   "title": "성산일출봉 등반",
     *   "description": "일출 관람 및 등반",
     *   "startTime": "06:00",
     *   "endTime": "08:00",
     *   "location": {
     *     "id": 1,
     *     "name": "성산일출봉",
     *     "address": "제주 서귀포시 성산읍",
     *     "latitude": 33.4586,
     *     "longitude": 126.9409
     *   },
     *   "previousLocation": null,
     *   "orderIndex": 1,
     *   "transportMethod": "도보",
     *   "estimatedCost": 5000
     * }
     * </pre>
     * 
     * 이동 경로 계산 예시:
     * <pre>
     * Activity 1: 숙소 (호텔)
     * Activity 2: 성산일출봉 (previousLocationId = 1)  ← 숙소에서 성산일출봉까지 이동
     * Activity 3: 우도 (previousLocationId = 2)        ← 성산일출봉에서 우도까지 이동
     * 
     * → Google Maps Directions API를 통해 각 구간의 이동 시간, 거리 계산 가능
     * </pre>
     */
    @Operation(summary = "활동 추가", description = "일별 일정에 활동을 추가합니다 (위치, 시간, 이동 경로)")
    @PostMapping("/{templateId}/days/{dayId}/activities")
    public ResponseEntity<?> addActivity(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "일정 ID") @PathVariable Long dayId,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.addActivity(user, templateId, dayId, request));
    }

    /**
     * 활동 수정
     * 
     * HTTP Method: PUT
     * URL: /api/templates/{templateId}/days/{dayId}/activities/{activityId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 활동 정보 수정 (제목, 시간, 위치 등)
     * - 순서 변경
     * - 이동 경로 재설정
     * 
     * 요청 예시:
     * <pre>
     * PUT /api/templates/1/days/1/activities/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * Content-Type: application/json
     * 
     * {
     *   "title": "성산일출봉 등반 및 사진 촬영",
     *   "description": "일출 관람 및 등반, 정상에서 사진 촬영",
     *   "startTime": "05:30",
     *   "endTime": "08:30",
     *   "locationId": 1,
     *   "previousLocationId": null,
     *   "orderIndex": 1,
     *   "transportMethod": "도보",
     *   "estimatedCost": 5000
     * }
     * </pre>
     */
    @Operation(summary = "활동 수정")
    @PutMapping("/{templateId}/days/{dayId}/activities/{activityId}")
    public ResponseEntity<?> updateActivity(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "일정 ID") @PathVariable Long dayId,
            @Parameter(description = "활동 ID") @PathVariable Long activityId,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(
                templateService.updateActivity(user, templateId, dayId, activityId, request));
    }

    /**
     * 활동 삭제
     * 
     * HTTP Method: DELETE
     * URL: /api/templates/{templateId}/days/{dayId}/activities/{activityId}
     * 인증: 필요 (JWT, 소유자만)
     * 
     * 역할:
     * - 활동 삭제
     * - 다른 활동의 이동 경로에 영향을 줄 수 있으므로 주의 필요
     *   (다음 활동의 previousLocationId가 삭제된 활동의 위치를 참조하는 경우)
     * 
     * 요청 예시:
     * <pre>
     * DELETE /api/templates/1/days/1/activities/1
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * </pre>
     * 
     * 성공 응답 (204 No Content):
     * (응답 본문 없음)
     * 
     * 주의사항:
     * <pre>
     * Activity 1: 숙소
     * Activity 2: 성산일출봉 (previousLocationId = 1)
     * Activity 3: 우도 (previousLocationId = 2)
     * 
     * → Activity 2를 삭제하면, Activity 3의 이동 경로가 유효하지 않게 됨
     * → 필요시 Activity 3의 previousLocationId를 1로 수정 필요
     * </pre>
     */
    @Operation(summary = "활동 삭제", description = "활동을 삭제합니다 (이동 경로에 영향을 줄 수 있음)")
    @DeleteMapping("/{templateId}/days/{dayId}/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "템플릿 ID") @PathVariable Long templateId,
            @Parameter(description = "일정 ID") @PathVariable Long dayId,
            @Parameter(description = "활동 ID") @PathVariable Long activityId) {
        templateService.deleteActivity(user, templateId, dayId, activityId);
        return ResponseEntity.noContent().build();
    }
}

