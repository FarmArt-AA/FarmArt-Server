package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.CreateDesignerProjectDto;
import cmc.farmart.controller.v1.user.dto.GetDesignerProfileIntroduceDto;
import cmc.farmart.controller.v1.user.dto.UpdateDesignerProfileImageDto;
import cmc.farmart.controller.v1.user.dto.UpdateDesignerProfileIntroduceDto;
import cmc.farmart.sevice.user.DesignerMyPageService;
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

@RequestMapping("/v1/my-page/designer")
@RequiredArgsConstructor
@RestController
public class DesignerMyPageController {

    private final DesignerMyPageService designerMyPageService;

    @Operation(summary = "디자이너 프로필 이미지 변경")
    @PostMapping(value = "/profileImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateDesignerProfileImageDto.Response> updateFarmerProfileImage(
            @Parameter(
                    description = "디자이너 프로필 이미지",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @Valid UpdateDesignerProfileImageDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(designerMyPageService.updateDesignerProfileImage(request));
    }

    @Operation(summary = "디자이너 프로필 조회: [닉네임, 프로필 제목, 작업 분야]")
    @GetMapping("/{userId}/introduce")
    public ResponseEntity<GetDesignerProfileIntroduceDto.Response> getFarmerProfileIntroduce(@Parameter(description = "사용자 PK") @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(designerMyPageService.getDesignerProfileIntroduce(userId));
    }

    @Operation(summary = "디자이너 프로필 수정: [닉네임, 프로필 제목, 작업 분야]")
    @PutMapping("/introduce")
    public ResponseEntity<UpdateDesignerProfileIntroduceDto.Response> updateFarmerProfileIntroduce(
            @RequestBody @Valid UpdateDesignerProfileIntroduceDto.Request request) {
        return ResponseEntity.status(HttpStatus.OK).body(designerMyPageService.updateDesignerProfileIntroduce(request));
    }

    @Operation(summary = "디자이너 프로젝트 등록")
    @PostMapping("/project")
    public ResponseEntity<CreateDesignerProjectDto.Response> createDesignerProject(
            CreateDesignerProjectDto.Request request) throws URISyntaxException {
        return ResponseEntity.status(HttpStatus.CREATED).body(designerMyPageService.createDesignerProject(request));
    }
}
