package cmc.farmart.controller.v1.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class UpdateFarmerProfileImageDto {

    @Getter
    @Setter
    public static class Request {

        @NotNull(message = "선택된 파일이 없습니다.")
        @Schema(description = "농부 프로필 이미지 필수", required = true, type = "file")
        private MultipartFile file;

        @NotNull(message = "사용자 PK 필수")
        @Schema(description = "사용자 PK")
        private Long userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter // builder가 @Getter로 필드를 조회해서 값을 맵핑하는 것 같다.
    public static class Response {

        @Schema(description = "농부 프로필 PK")
        private Long farmerProfileId;

        @Schema(description = "농부 프로필 이미지")
        private String path;

        @Schema(description = "사용자 PK")
        private Long userId;

    }
}
