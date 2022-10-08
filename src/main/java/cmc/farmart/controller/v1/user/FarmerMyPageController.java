package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.UpdateFarmerProfileImageDto;
import cmc.farmart.sevice.user.FarmerMyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/my-page")
@RequiredArgsConstructor
@RestController
public class FarmerMyPageController {

    private final FarmerMyPageService farmerMyPageService;

    @Operation(description = "농부 프로필 이미지 변경")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateFarmerProfileImageDto.Response> updateFarmerProfileImage(UpdateFarmerProfileImageDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(farmerMyPageService.updateFarmerProfileImage(request));
    }
}
