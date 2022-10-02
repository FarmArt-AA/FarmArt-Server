package cmc.farmart.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum JobTitle {
    FARMER(1, "농부"),
    DESIGNER(2, "디자이너");

    private final Integer jobTitleCode;
    private final String jobTitleName;

    public static JobTitle ofJobTitleCode(int jobTitleCode) {
        return Arrays.stream(values())
                .filter(jobTitle -> jobTitle.getJobTitleCode() == jobTitleCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 JobTitle 입니다."));
    }

}
