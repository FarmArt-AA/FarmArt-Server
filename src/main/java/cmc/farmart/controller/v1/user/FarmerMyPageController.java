package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.*;
import cmc.farmart.repository.user.CreateFarmProfileImageDto;
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
    @PostMapping(value = "/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    @PutMapping("/introduce")
    public ResponseEntity<UpdateFarmerProfileIntroduceDto.Response> updateFarmerProfileIntroduce(
            @RequestBody @Valid UpdateFarmerProfileIntroduceDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerProfileIntroduce(request));
    }

    @Operation(summary = "농부 재배 중인 작물 조회")
    @GetMapping("/{userId}/crops")
    public ResponseEntity<GetCropDto.Response> getFarmerCrops(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.getFarmerCrops(userId));
    }

    @Operation(summary = "농부 재배 중인 작물 수정")
    @PutMapping("/crop")
    public ResponseEntity<UpdateCropDto.Response> updateFarmerCrop(UpdateCropDto.Request request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerCrop(request));
    }

    @Operation(summary = "농부 재배중인 작물 추가하기")
    @PostMapping("/crop")
    public ResponseEntity<CreateCropDto.Response> createFarmerCrop(
            @Parameter(
                    description = "농부 재배 중인 작물 이미지 리스트",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @Valid CreateCropDto.Request request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmerMyPageService.createFarmerCrop(request));
    }

    @Operation(summary = "농부의 농가 사진 리스트 조회")
    @GetMapping("/{userId}/farmImages")
    public ResponseEntity<GetFarmProfileImagesDto.Response> getFarmProfileImages(@PathVariable Long userId) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.getFarmProfileImages(userId));
    }

    @Operation(summary = "농부의 농가 사진 등록")
    @PostMapping("/farm")
    public ResponseEntity<CreateFarmProfileImageDto.Response> createFarmProfileImage(
            @Parameter(
                    description = "농부 재배 중인 작물 이미지 리스트",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @Valid CreateFarmProfileImageDto.Request request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.createFarmProfileImage(request));
    }

    @Operation(summary = "농부의 농가 사진 삭제")
    @DeleteMapping("/{userId}/farm/{farmProfileImageId}")
    public ResponseEntity<Void> deleteFarmProfileImage(
            @PathVariable Long userId,
            @PathVariable Long farmProfileImageId) {

        farmerMyPageService.deleteFarmProfileImage(userId, farmProfileImageId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "농부 프로필의 링크 조회")
    @GetMapping("/{userId}/links")
    public ResponseEntity<GetFarmerProfileLinkDto.Response> getFarmerProfileLinks(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.getFarmerProfileLinks(userId));
    }

    @Operation(summary = "농부 프로필의 링크 추가")
    @PostMapping("/links")
    public ResponseEntity<CreateFarmerProfileLinkDto.Response> createFarmerProfileLink(
            @RequestBody @Valid CreateFarmerProfileLinkDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmerMyPageService.createFarmerProfileLink(request));
    }

    @Operation(summary = "농부 프로필의 링크 수정")
    @PutMapping("/links")
    public ResponseEntity<UpdateFarmerProfileLinkDto.Response> updateFarmerProfileLink(
            @RequestBody @Valid UpdateFarmerProfileLinkDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerProfileLink(request));
    }

}

