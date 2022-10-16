package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.CreateDesignerProjectDto;
import cmc.farmart.controller.v1.user.dto.GetDesignerProfileIntroduceDto;
import cmc.farmart.controller.v1.user.dto.UpdateDesignerProfileImageDto;
import cmc.farmart.controller.v1.user.dto.UpdateDesignerProfileIntroduceDto;
import cmc.farmart.entity.User;
import cmc.farmart.entity.designer.*;
import cmc.farmart.repository.user.DesignerProfileRepository;
import cmc.farmart.repository.user.DesignerProjectImageRepository;
import cmc.farmart.repository.user.DesignerProjectRepository;
import cmc.farmart.repository.user.DesignerWorkAreaRepository;
import cmc.farmart.sevice.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DesignerMyPageService {

    @Value("${farmart.project.document.bucket}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expire-millisecond}")
    private Long presignedUrlExpireMillisecond;

    private final String DESIGNER_ORIGIN_S3_PREFIX_OBJECT_KEY = "/designer/image";


    private final DesignerProfileRepository designerProfileRepository;
    private final DesignerWorkAreaRepository designerWorkAreaRepository;
    private final DesignerProjectRepository designerProjectRepository;
    private final DesignerProjectImageRepository designerProjectImageRepository;
    private final UserService userService;
    private final S3Service s3Service;

    public UpdateDesignerProfileImageDto.Response updateDesignerProfileImage(final UpdateDesignerProfileImageDto.Request request) {
        User user = userService.getUserById(request.getUserId());

        String bucketKey = s3Service.getBucketKey(request.getFile(), DESIGNER_ORIGIN_S3_PREFIX_OBJECT_KEY);

        // TODO:: Need Code Refactoring
        DesignerProfile designerProfile = getDesignerProfile(user);
        if (Objects.nonNull(designerProfile.getDesignerProfileImagePath())) { // 서버로부터 이미지를 받은 이후부터는 s3 기존 이미지를 삭제하고 저장한다.
            s3Service.delete(bucketName, designerProfile.getDesignerProfileImagePath());
        }
        designerProfile.setDesignerProfileImagePath(bucketKey);

        try {
            // 이미지 등록
            s3Service.put(bucketName, bucketKey, request.getFile().getInputStream());

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);

            return UpdateDesignerProfileImageDto.Response.builder()
                    .userId(user.getId())
                    .path(downloadUrl)
                    .build();

        } catch (Exception ex) {
            s3Service.delete(bucketName, bucketKey);
            log.error(ex.getMessage());
            throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public GetDesignerProfileIntroduceDto.Response getDesignerProfileIntroduce(Long userId) {
        User user = userService.getUserById(userId);
        Optional<DesignerProfile> designerProfile = designerProfileRepository.findByUser(user);
        if (Boolean.FALSE.equals(designerProfile.isPresent())) {
            designerProfileRepository.save(new DesignerProfile(user));
            return GetDesignerProfileIntroduceDto.Response.builder()
                    .nickName(user.getUserNickName())
                    .build();
        } else {
            return GetDesignerProfileIntroduceDto.Response.builder()
                    .nickName(user.getUserNickName())
                    .designerProfileTitle(designerProfile.get().getDesignerProfileTitle())
                    .designerProfileIntroduce(designerProfile.get().getDesignerProfileIntroduce())
                    .designerWorkAreaTypes(
                            designerWorkAreaRepository.findAllByDesignerProfile(designerProfile.get()).stream()
                                    .map(designerWorkArea -> GetDesignerProfileIntroduceDto.DesignerWorkAreaTypes.builder()
                                            .designerWorkAreaType(designerWorkArea.getDesignerWorkAreaType())
                                            .build()).collect(Collectors.toList())).build();
        }
    }

    public UpdateDesignerProfileIntroduceDto.Response updateDesignerProfileIntroduce(UpdateDesignerProfileIntroduceDto.Request request) {
        User user = userService.getUserById(request.getUserId());
        DesignerProfile designerProfile = getDesignerProfile(user);

        // 디자이너 프로필 수정
        user.setUserNickName(request.getNickName());
        designerProfile.setDesignerProfile(request.getDesignerProfileTitle(), request.getDesignerProfileIntroduce());

        // 기존 Designer WorkArea 삭제
        designerWorkAreaRepository.deleteAllByDesignerProfile(designerProfile);

        // request로 받은 디자이너 작업 유형 -> DB에 저장
        for (DesignerWorkAreaType designerWorkAreaType : request.getDesignerWorkAreaTypes()) {
            DesignerWorkArea designerWorkArea = DesignerWorkArea.builder()
                    .designerProfile(designerProfile)
                    .designerWorkAreaType(designerWorkAreaType)
                    .build();
            designerWorkAreaRepository.save(designerWorkArea);
        }

        List<UpdateDesignerProfileIntroduceDto.DesignerWorkAreaTypes> designerWorkAreaTypesDto = designerWorkAreaRepository.findAllByDesignerProfile(designerProfile).stream()
                .map(designerWorkArea -> UpdateDesignerProfileIntroduceDto.DesignerWorkAreaTypes.builder()
                        .designerWorkAreaType(designerWorkArea.getDesignerWorkAreaType())
                        .build()).collect(Collectors.toList());

        return UpdateDesignerProfileIntroduceDto.Response.builder()
                .userId(user.getId())
                .nickName(user.getUserNickName())
                .designerProfileTitle(designerProfile.getDesignerProfileTitle())
                .designerProfileIntroduce(designerProfile.getDesignerProfileIntroduce())
                .designerWorkAreaTypes(designerWorkAreaTypesDto)
                .build();
    }

    private DesignerProfile getDesignerProfile(User user) {
        return designerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_DESIGNER_PROFILE));
    }

    public CreateDesignerProjectDto.Response createDesignerProject(CreateDesignerProjectDto.Request request) throws URISyntaxException {

        User user = userService.getUserById(request.getUserId());
        Optional<DesignerProfile> designerProfile = designerProfileRepository.findByUser(user);
        if (Boolean.FALSE.equals(designerProfile.isPresent())) {
            throw new FarmartException(Status.NOT_EXISTS_DESIGNER_PROFILE);
        }
        // request로 받은 정보를 토대로 디자이너 프로젝트르 생성한다.
        DesignerProject designerProject = DesignerProject.builder()
                .designerProjectTitle(request.getDesignerProjectTitle())
                .designerProjectStartDate(request.getDesignerProjectStartDate())
                .designerProjectEndDate(request.getDesignerProjectEndDate())
                .designerProjectIntroduce(request.getDesignerProjectIntroduce())
                .build();
        designerProjectRepository.save(designerProject);

        if (request.getDesignerProjectImages().size() > 3) { // 이미지 3개 이상 등록 불가능
            throw new FarmartException(Status.MAX_UPLOAD_SIZE_EXCEEDED);
        }
        ArrayList<CreateDesignerProjectDto.DesignerProjectImageURL> designerProjectImageURLS = new ArrayList<>(); // 재배 중인 작물 사진을 담을 리스트
        for (CreateDesignerProjectDto.DesignerProjectImageFiles designerProjectImage : request.getDesignerProjectImages()) {
            String bucketKey = s3Service.getBucketKey(designerProjectImage.getDesignerProjectImageFile(), DESIGNER_ORIGIN_S3_PREFIX_OBJECT_KEY);
            try {
                // 이미지 등록
                s3Service.put(bucketName, bucketKey, designerProjectImage.getDesignerProjectImageFile().getInputStream());
            } catch (Exception ex) {
                s3Service.delete(bucketName, bucketKey);
                log.error(ex.getMessage());
                throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
            }
            DesignerProjectImage newDesignerProjectImage = DesignerProjectImage.builder()
                    .designerProject(designerProject)
                    .designerProjectImagePath(bucketKey)
                    .build();
            designerProjectImageRepository.save(newDesignerProjectImage);
            String downloadURL = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);
            CreateDesignerProjectDto.DesignerProjectImageURL designerProjectImageURL = CreateDesignerProjectDto.DesignerProjectImageURL.builder()
                    .designerProjectImageId(newDesignerProjectImage.getId())
                    .designerProjectImageURL(downloadURL)
                    .build();
            designerProjectImageURLS.add(designerProjectImageURL);
        }

        return CreateDesignerProjectDto.Response.builder()
                .designerProjectTitle(designerProject.getDesignerProjectTitle())
                .designerProjectStartDate(designerProject.getDesignerProjectStartDate())
                .designerProjectEndDate(designerProject.getDesignerProjectEndDate())
                .designerProjectIntroduce(designerProject.getDesignerProjectIntroduce())
                .designerProjectImages(designerProjectImageURLS)
                .build();
    }
}

