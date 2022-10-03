package cmc.farmart.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DesignerWorkAreaType {
    LOGO_DESIGN(1, "로고 디자인"),
    PACKAGE_LABEL(2, "패키지 라벨"),
    SNS_DESIGN(3, "SNS 디자인");

    private final Integer designerWorkAreaTypeCode;
    private final String designerWorkAreaTypeName;

    public static DesignerWorkAreaType ofJobTitleCode(int designerWorkAreaTypeCode) {
        return Arrays.stream(values())
                .filter(designerWorkAreaType -> designerWorkAreaType.getDesignerWorkAreaTypeCode() == designerWorkAreaTypeCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 작업 분야입니다."));
    }
    }
