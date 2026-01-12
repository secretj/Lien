package com.lien.service;

import com.lien.dto.request.LocationRequest;
import com.lien.dto.response.LocationResponse;
import com.lien.entity.Location;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import com.lien.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Location Service (위치 서비스)
 * ==============================================================================
 * 
 * 역할:
 * - 위치 정보 관리 비즈니스 로직
 * - Google Maps API 연동을 위한 위치 데이터 제공
 * - 공개/비공개 위치 관리
 * - 카테고리 및 키워드 검색
 * 
 * 이점:
 * 1. 위치 공유 기능 (공개/비공개)
 * 2. 다양한 검색 조건 (카테고리, 키워드)
 * 3. 권한 검증 (소유자만 수정/삭제)
 * 4. Google Maps 연동 준비
 * 
 * 위치 카테고리:
 * - ATTRACTION: 관광지 (성산일출봉, 한라산)
 * - HOTEL: 숙소 (호텔, 리조트)
 * - AIRPORT: 공항 (제주국제공항)
 * - RESTAURANT: 음식점 (카페, 식당)
 * - MASSAGE: 마사지/스파
 * - SHOPPING: 쇼핑 (면세점, 시장)
 * 
 * 공개/비공개 설정:
 * <pre>
 * isPublic = true:  모든 사용자가 검색 및 사용 가능
 * isPublic = false: 등록한 사용자만 사용 가능
 * </pre>
 * 
 * Google Maps 연동 예시:
 * <pre>
 * {@code
 * // 위치 데이터로 지도 마커 생성
 * LocationResponse location = locationService.getLocation(user, locationId);
 * 
 * // Google Maps JavaScript API
 * var marker = new google.maps.Marker({
 *     position: {
 *         lat: location.getLatitude(),
 *         lng: location.getLongitude()
 *     },
 *     map: map,
 *     title: location.getName()
 * });
 * 
 * // 경로 계산 (이전 위치 → 현재 위치)
 * var directionsService = new google.maps.DirectionsService();
 * directionsService.route({
 *     origin: {lat: prevLat, lng: prevLng},
 *     destination: {lat: location.getLatitude(), lng: location.getLongitude()},
 *     travelMode: 'DRIVING'
 * }, callback);
 * }
 * </pre>
 * 
 * @see Location
 * @see LocationRepository
 * @see LocationCategory
 */
@Service
@RequiredArgsConstructor
public class LocationService
{

    private final LocationRepository locationRepository;

    /**
     * 위치 생성
     * 
     * 역할:
     * - 새로운 위치 등록
     * - Google Maps에서 가져온 데이터 저장
     * - 공개/비공개 설정
     * 
     * 처리 흐름:
     * 1. Location 엔티티 생성
     * 2. 위도/경도, 주소 등 저장
     * 3. 데이터베이스 저장
     * 4. DTO로 변환하여 반환
     * 
     * Google Maps Places API 연동 예시:
     * <pre>
     * {@code
     * // 1. Google Maps Places API로 장소 검색
     * PlacesSearchResponse response = placesApi.textSearchQuery(context, "성산일출봉")
     *     .await();
     * 
     * PlaceDetails place = response.results[0];
     * 
     * // 2. 검색 결과로 위치 생성
     * LocationRequest request = LocationRequest.builder()
     *     .name(place.name)
     *     .category(LocationCategory.ATTRACTION)
     *     .latitude(place.geometry.location.lat)
     *     .longitude(place.geometry.location.lng)
     *     .address(place.formattedAddress)
     *     .description(place.types[0])
     *     .isPublic(true)
     *     .build();
     * 
     * LocationResponse location = locationService.createLocation(user, request);
     * }
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * LocationRequest request = LocationRequest.builder()
     *     .name("성산일출봉")
     *     .category(LocationCategory.ATTRACTION)
     *     .latitude(33.4584)
     *     .longitude(126.9423)
     *     .address("제주특별자치도 서귀포시 성산읍 성산리")
     *     .description("유네스코 세계자연유산")
     *     .isPublic(true)  // 공개 위치로 등록
     *     .build();
     * 
     * LocationResponse response = locationService.createLocation(user, request);
     * System.out.println("위치 ID: " + response.getId());
     * }
     * </pre>
     * 
     * @param user 등록 사용자
     * @param request 위치 정보
     * @return LocationResponse 생성된 위치
     */
    @Transactional
    public LocationResponse createLocation(User user, LocationRequest request)
    {
        Location location = Location.builder()
                .user(user)
                .name(request.getName())
                .category(request.getCategory())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .description(request.getDescription())
                .isPublic(request.getIsPublic())
                .build();

        location = locationRepository.save(location);
        return toResponse(location);
    }

    /**
     * 위치 목록 조회 (필터링)
     * 
     * 역할:
     * - 공개 위치 + 내 위치 조회
     * - 카테고리 필터링 (선택사항)
     * - 키워드 검색 (선택사항)
     * 
     * 검색 로직:
     * 1. 기본: 공개 위치 + 내가 등록한 위치
     * 2. 카테고리 필터: 특정 카테고리만
     * 3. 키워드 검색: 이름에 포함된 위치
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 모든 공개 위치 + 내 위치
     * List<LocationResponse> all = locationService.getLocations(user, null, null);
     * 
     * // 호텔만 검색
     * List<LocationResponse> hotels = locationService.getLocations(
     *     user, LocationCategory.HOTEL, null
     * );
     * 
     * // "제주" 포함 검색
     * List<LocationResponse> jeju = locationService.getLocations(
     *     user, null, "제주"
     * );
     * 
     * // 제주의 관광지만 검색
     * List<LocationResponse> jejuAttractions = locationService.getLocations(
     *     user, LocationCategory.ATTRACTION, "제주"
     * );
     * 
     * // 결과:
     * // - 성산일출봉 (공개, 관광지, "제주" 포함)
     * // - 제주 신라호텔 (내 위치, 호텔, "제주" 포함) <- 카테고리 불일치로 제외
     * }
     * </pre>
     * 
     * @param user 현재 사용자
     * @param category 카테고리 필터 (null이면 전체)
     * @param keyword 검색 키워드 (null이면 검색 안 함)
     * @return List<LocationResponse> 조건에 맞는 위치 목록
     */
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocations(
        User user,
        LocationCategory category,
        String keyword
    ) {
        return locationRepository.findByUserOrPublicWithFilters(user, category, keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 위치 상세 조회
     * 
     * 역할:
     * - 위치 ID로 상세 정보 조회
     * - 권한 검증 (공개 위치 또는 내 위치만)
     * 
     * 권한:
     * - 공개 위치 (isPublic = true): 모든 사용자 조회 가능
     * - 비공개 위치 (isPublic = false): 등록한 사용자만 조회 가능
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 공개 위치 조회 (누구나 가능)
     * LocationResponse location = locationService.getLocation(user, 1L);
     * 
     * // 비공개 위치 조회 (소유자만 가능)
     * try {
     *     LocationResponse privateLocation = locationService.getLocation(otherUser, 2L);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); // "권한이 없습니다"
     * }
     * 
     * // Activity에서 위치 정보 사용
     * Activity activity = ...
     * LocationResponse loc = locationService.getLocation(user, activity.getLocation().getId());
     * System.out.println("위치: " + loc.getName());
     * System.out.println("좌표: " + loc.getLatitude() + ", " + loc.getLongitude());
     * }
     * </pre>
     * 
     * @param user 현재 사용자
     * @param locationId 위치 ID
     * @return LocationResponse 위치 정보
     * @throws IllegalArgumentException 위치가 없거나 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public LocationResponse getLocation(User user, Long locationId)
    {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없습니다"));

        // 권한 검증: 공개 위치이거나 본인의 위치만 조회 가능
        if (!location.getIsPublic()
            && (location.getUser() == null || !location.getUser().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        return toResponse(location);
    }

    /**
     * 위치 수정
     * 
     * 역할:
     * - 위치 정보 업데이트
     * - 소유자만 수정 가능
     * 
     * 수정 가능한 항목:
     * - 이름, 카테고리
     * - 위도, 경도
     * - 주소, 설명
     * - 공개/비공개 설정
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 위치 정보 업데이트
     * LocationRequest request = LocationRequest.builder()
     *     .name("제주 신라호텔 (리모델링)")  // 이름 변경
     *     .category(LocationCategory.HOTEL)
     *     .latitude(33.2514)
     *     .longitude(126.5606)
     *     .address("제주시 중문관광로72번길")
     *     .description("수영장 신규 오픈")       // 설명 추가
     *     .isPublic(true)                       // 공개로 변경
     *     .build();
     * 
     * LocationResponse updated = locationService.updateLocation(user, locationId, request);
     * System.out.println("업데이트: " + updated.getName());
     * 
     * // 권한 없는 사용자 수정 시도
     * try {
     *     locationService.updateLocation(otherUser, locationId, request);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); 
     *     // "위치를 찾을 수 없거나 권한이 없습니다"
     * }
     * }
     * </pre>
     * 
     * @param user 현재 사용자
     * @param locationId 위치 ID
     * @param request 수정할 정보
     * @return LocationResponse 수정된 위치
     * @throws IllegalArgumentException 위치가 없거나 권한이 없는 경우
     */
    @Transactional
    public LocationResponse updateLocation(
        User user,
        Long locationId,
        LocationRequest request
    ) {
        // 소유자 확인
        Location location = locationRepository.findByIdAndUser(locationId, user)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없거나 권한이 없습니다"));

        // 위치 정보 업데이트
        location.setName(request.getName());
        location.setCategory(request.getCategory());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());
        location.setDescription(request.getDescription());
        location.setIsPublic(request.getIsPublic());

        return toResponse(locationRepository.save(location));
    }

    /**
     * 위치 삭제
     * 
     * 역할:
     * - 위치 정보 삭제
     * - 소유자만 삭제 가능
     * 
     * 주의사항:
     * - Activity에서 사용 중인 위치 삭제 시 외래 키 오류 발생 가능
     * - 삭제 전 사용 여부 확인 권장
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 위치 삭제
     * locationService.deleteLocation(user, locationId);
     * System.out.println("위치 삭제 완료");
     * 
     * // 사용 중인 위치 삭제 시도 (외래 키 오류)
     * try {
     *     locationService.deleteLocation(user, usedLocationId);
     * } catch (DataIntegrityViolationException e) {
     *     System.out.println("이 위치는 활동에서 사용 중입니다");
     * }
     * 
     * // 삭제 전 사용 여부 확인 (권장)
     * List<Activity> activities = activityRepository.findByLocationId(locationId);
     * if (!activities.isEmpty()) {
     *     throw new IllegalStateException(
     *         "이 위치는 " + activities.size() + "개의 활동에서 사용 중입니다"
     *     );
     * }
     * locationService.deleteLocation(user, locationId);
     * }
     * </pre>
     * 
     * @param user 현재 사용자
     * @param locationId 위치 ID
     * @throws IllegalArgumentException 위치가 없거나 권한이 없는 경우
     */
    @Transactional
    public void deleteLocation(User user, Long locationId)
    {
        // 소유자 확인
        Location location = locationRepository.findByIdAndUser(locationId, user)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없거나 권한이 없습니다"));

        locationRepository.delete(location);
    }

    /**
     * Entity → DTO 변환
     * 
     * 역할:
     * - Location 엔티티를 LocationResponse DTO로 변환
     * - 필요한 정보만 노출
     * 
     * 변환 항목:
     * - 기본 정보: id, name, category
     * - 좌표: latitude, longitude
     * - 상세: address, description
     * - 설정: isPublic
     * - 메타: createdAt
     * 
     * @param location Location 엔티티
     * @return LocationResponse DTO
     */
    private LocationResponse toResponse(Location location)
    {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .category(location.getCategory())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .description(location.getDescription())
                .isPublic(location.getIsPublic())
                .createdAt(location.getCreatedAt())
                .build();
    }
}

