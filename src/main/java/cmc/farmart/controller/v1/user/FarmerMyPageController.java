package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.*;
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
import java.net.URISyntaxException;

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

    @Operation(summary = "농부 재배 중인 작물 조회")
    @GetMapping("/{userId}/crop")
    public ResponseEntity<GetCropDto.Response> getFarmerCrops(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.getFarmerCrops(userId));
    }

    @Operation(summary = "농부 재배 중인 작물 수정")
    @PutMapping("/{userId}/crop/{cropId}")
    public ResponseEntity<UpdateCropDto.Response> updateFarmerCrop(
            @PathVariable Long userId,
            @PathVariable Long cropId,
            UpdateCropDto.Request request
    ) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerCrop(userId, cropId, request));
    }

    @Operation(summary = "농부 재배중인 작물 추가하기")
    @PostMapping("/{userId}/crop")
    public ResponseEntity<CreateCropDto.Response> createFarmerCrop(
            @PathVariable String userId,
            @Parameter(
                    description = "농부 재배 중인 작물 이미지 리스트",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @Valid CreateCropDto.Request request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmerMyPageService.createFarmerCrop(userId, request));
    }

}
