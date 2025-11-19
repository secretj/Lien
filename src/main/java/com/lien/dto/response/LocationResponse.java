package com.lien.dto.response;

import com.lien.entity.LocationCategory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationResponse {

    private Long id;
    private String name;
    private LocationCategory category;
    private Double latitude;
    private Double longitude;
    private String address;
    private String description;
    private Boolean isPublic;
    private LocalDateTime createdAt;
}

