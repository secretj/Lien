package com.lien.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivityRequest {

    @NotBlank
    @Size(max = 50)
    private String time;

    @NotBlank
    @Size(max = 300)
    private String description;

    @NotNull
    private Long locationId;

    private Long previousLocationId;

    @NotNull
    @Min(0)
    private Integer orderIndex;
}

