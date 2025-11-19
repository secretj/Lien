package com.lien.dto.request;

import com.lien.entity.LocationCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocationRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 200)
    private String name;

    @NotNull(message = "카테고리는 필수입니다")
    private LocationCategory category;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 500)
    private String address;

    private String description;

    private Boolean isPublic = false;
}

