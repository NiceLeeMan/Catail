package com.catail.backend.catalyst.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCatalystRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        String content,

        @NotEmpty(message = "업종은 1개 이상 선택해야 합니다.")
        @Size(max = 10, message = "업종은 최대 10개까지 선택할 수 있습니다.")
        List<Long> industryIds,

        @NotBlank(message = "상태값은 필수입니다.")
        String status
) {
}
