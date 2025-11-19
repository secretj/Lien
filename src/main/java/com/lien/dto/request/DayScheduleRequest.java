package com.lien.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DayScheduleRequest {

    @NotNull
    @Min(1)
    private Integer dayNumber;

    @NotNull
    private LocalDate date;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 20)
    private String color;
}

