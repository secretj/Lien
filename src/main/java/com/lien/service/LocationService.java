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

@Service
@RequiredArgsConstructor
public class LocationService
{

    private final LocationRepository locationRepository;

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

    @Transactional(readOnly = true)
    public LocationResponse getLocation(User user, Long locationId)
    {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없습니다"));

        // Public이거나 본인의 위치만 조회 가능
        if (!location.getIsPublic()
            && (location.getUser() == null || !location.getUser().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("권한이 없습니다");
        }

        return toResponse(location);
    }

    @Transactional
    public LocationResponse updateLocation(
        User user,
        Long locationId,
        LocationRequest request
    ) {
        Location location = locationRepository.findByIdAndUser(locationId, user)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없거나 권한이 없습니다"));

        location.setName(request.getName());
        location.setCategory(request.getCategory());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());
        location.setDescription(request.getDescription());
        location.setIsPublic(request.getIsPublic());

        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(User user, Long locationId)
    {
        Location location = locationRepository.findByIdAndUser(locationId, user)
                .orElseThrow(() -> new IllegalArgumentException("위치를 찾을 수 없거나 권한이 없습니다"));

        locationRepository.delete(location);
    }

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

