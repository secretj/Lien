package com.lien.controller;

import com.lien.dto.request.LocationRequest;
import com.lien.dto.response.LocationResponse;
import com.lien.entity.LocationCategory;
import com.lien.entity.User;
import com.lien.security.CurrentUser;
import com.lien.service.LocationService;
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

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(
            @CurrentUser User user,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createLocation(user, request));
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations(
            @CurrentUser User user,
            @RequestParam(required = false) LocationCategory category,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                locationService.getLocations(user, category, keyword));
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<LocationResponse> getLocation(
            @CurrentUser User user,
            @PathVariable Long locationId) {
        return ResponseEntity.ok(locationService.getLocation(user, locationId));
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<LocationResponse> updateLocation(
            @CurrentUser User user,
            @PathVariable Long locationId,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(
                locationService.updateLocation(user, locationId, request));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(
            @CurrentUser User user,
            @PathVariable Long locationId) {
        locationService.deleteLocation(user, locationId);
        return ResponseEntity.noContent().build();
    }
}

