package com.lien.controller;

import com.lien.dto.request.LocationRequest;
import com.lien.dto.response.LocationResponse;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import com.lien.security.CurrentUser;
import com.lien.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==============================================================================
 * Location Controller (위치 컨트롤러)
 * ==============================================================================
 * 
 * 역할:
 * - 여행지, 숙소, 음식점 등 위치 정보 관리
 * - 공개/비공개 위치 정보 조회
 * - 카테고리 및 키워드 검색
 * - Google Maps API 연동 지원
 * 
 * Base URL: /api/locations
 * 인증: JWT 토큰 필요
 * 
 * 주요 기능:
 * - 위치 생성/조회/수정/삭제
 * - 카테고리별 필터링 (ATTRACTION, ACCOMMODATION, RESTAURANT 등)
 * - 키워드 검색 (이름, 주소)
 * - 소유자 권한 검증
 * 
 * @see LocationService
 * @see LocationCategory
 */
@Tag(name = "3. 위치", description = "여행지/숙소/음식점 관리 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(
        summary = "위치 생성",
        description = "새로운 위치 정보를 등록합니다 (Google Maps API 연동 가능)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "위치 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @Parameter(hidden = true) @CurrentUser User user,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createLocation(user, request));
    }

    @Operation(
        summary = "위치 목록 조회",
        description = """
            위치 목록을 조회합니다.
            - 카테고리 필터링 가능 (ATTRACTION, ACCOMMODATION, RESTAURANT, CAFE, SHOPPING, TRANSPORT)
            - 키워드 검색 가능 (이름, 주소)
            - 본인의 비공개 위치 + 모든 공개 위치 조회
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "카테고리 필터 (ATTRACTION, ACCOMMODATION, RESTAURANT, CAFE, SHOPPING, TRANSPORT)", example = "ATTRACTION")
            @RequestParam(required = false) LocationCategory category,
            @Parameter(description = "키워드 검색 (이름, 주소)", example = "성산일출봉")
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                locationService.getLocations(user, category, keyword));
    }

    @Operation(
        summary = "위치 상세 조회",
        description = "위치의 상세 정보를 조회합니다 (Google Maps 좌표 포함)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (비공개 위치)"),
        @ApiResponse(responseCode = "404", description = "위치를 찾을 수 없음")
    })
    @GetMapping("/{locationId}")
    public ResponseEntity<LocationResponse> getLocation(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "위치 ID", example = "1") @PathVariable Long locationId) {
        return ResponseEntity.ok(locationService.getLocation(user, locationId));
    }

    @Operation(
        summary = "위치 수정",
        description = "위치 정보를 수정합니다 (소유자만 가능)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (소유자가 아님)"),
        @ApiResponse(responseCode = "404", description = "위치를 찾을 수 없음")
    })
    @PutMapping("/{locationId}")
    public ResponseEntity<LocationResponse> updateLocation(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "위치 ID") @PathVariable Long locationId,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(
                locationService.updateLocation(user, locationId, request));
    }

    @Operation(
        summary = "위치 삭제",
        description = "위치를 삭제합니다 (소유자만 가능, 활동에서 참조 중인 경우 삭제 실패)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "위치를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "다른 활동에서 참조 중")
    })
    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(
            @Parameter(hidden = true) @CurrentUser User user,
            @Parameter(description = "위치 ID") @PathVariable Long locationId) {
        locationService.deleteLocation(user, locationId);
        return ResponseEntity.noContent().build();
    }
}

