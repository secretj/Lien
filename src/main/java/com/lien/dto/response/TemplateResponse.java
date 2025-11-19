package com.lien.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateResponse {

    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String accommodation;
    private String transportation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

