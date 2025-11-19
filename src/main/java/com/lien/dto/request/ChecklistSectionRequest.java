package com.lien.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class ChecklistSectionRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100)
    private String title;

    @Size(max = 10)
    private String icon;

    @NotNull
    @Min(0)
    private Integer orderIndex;

    @NotEmpty(message = "항목은 최소 1개 이상이어야 합니다")
    @Valid
    private List<ChecklistItemDto> items;

    @Data
    public static class ChecklistItemDto {

        @NotBlank
        @Size(max = 200)
        private String label;

        @NotNull
        @Min(0)
        private Integer orderIndex;
    }
}

