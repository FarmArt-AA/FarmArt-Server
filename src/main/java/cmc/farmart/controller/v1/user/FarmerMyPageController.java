package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.GetFarmerProfileIntroduceDto;
import cmc.farmart.controller.v1.user.dto.UpdateFarmerProfileImageDto;
import cmc.farmart.controller.v1.user.dto.UpdateFarmerProfileIntroduceDto;
import cmc.farmart.sevice.user.FarmerMyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1/my-page/farmer")
@RequiredArgsConstructor
@RestController
public class FarmerMyPageController {

    private final FarmerMyPageService farmerMyPageService;

    @Operation(summary = "농부 프로필 이미지 변경")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateFarmerProfileImageDto.Response> updateFarmerProfileImage(
            @Parameter(
                    description = "농부 프로필 이미지",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @Valid UpdateFarmerProfileImageDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerProfileImage(request));
    }

    @Operation(summary = "농부 프로필 조회: [닉네임, 농가명, 농사 지역, 소개]")
    @GetMapping("/{userId}/introduce")
    public ResponseEntity<GetFarmerProfileIntroduceDto.Response> getFarmerProfileIntroduce(@Parameter(description = "사용자 PK") @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.getFarmerProfileIntroduce(userId));
    }

    @Operation(summary = "농부 프로필 수정: [닉네임, 농가명, 농사 지역, 소개]")
    @PutMapping("/{userId}/introduce")
    public ResponseEntity<UpdateFarmerProfileIntroduceDto.Response> updateFarmerProfileIntroduce(
            @RequestBody UpdateFarmerProfileIntroduceDto.Request request,
            @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerProfileIntroduce(userId, request));
    }

}