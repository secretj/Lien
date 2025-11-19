package com.lien.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TemplateCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "목적지는 필수입니다")
    @Size(max = 200)
    private String destination;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    @NotNull(message = "총 일수는 필수입니다")
    @Min(1)
    private Integer totalDays;

    @Size(max = 200)
    private String accommodation;

    @Size(max = 200)
    private String transportation;
}

