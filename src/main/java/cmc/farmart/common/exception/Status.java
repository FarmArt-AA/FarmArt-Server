package cmc.farmart.common.exception;

public enum Status {
    BAD_REQUEST(400, "NBBDV", "bad request"),
    NOT_FOUND(404, "OFHQM", "Not Found"),
    METHOD_NOT_ALLOWED(405, "N3KG0", "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "HM9U3", "internal server error"),
    ACCESS_DENIED(403, "KDQKN", "접근불가"),
    ALREADY_VERIFY_VERIFICATION_CODE(400, "KEBXI", "인증번호 인증이 완료됨"),
    ALREADY_USED_VERIFICATION_CODE(400, "J4KXB", "인증번호 사용이 완료됨"),
    INCOMPLETE_VERIFICATION_CODE(401, "KSPOD", "인증 미완료"),
    MISMATCH_VERIFICATION_CODE(400, "QMAEH", "인증번호를 확인 요망"),
    EXPIRED_VERIFICATION_CODE(400, "HSLEK", "인증시간 만료"),
    INVALID_TOKEN(401, "MXQAV", "유효하지 않은 토큰"),
    EXPIRED_TOKEN(401, "ISMUD", "만료된 토큰"),
    IMAGE_FILE_ONLY(400, "DLSXV", "png, jpg, jpeg, gif만 업로드 가능"),
    NON_SIGN_IN(401, "TCJAN", "비로그인"),
    MISMATCH_EMAIL_PASSWORD(401, "HEXLW", "이메일 또는 비밀번호 확인"),
    INACTIVE_USER(401, "JEKXW", "비활성 유저"),
    EXIST_EMAIL(400, "XPQOW", "이미 존재하는 이메일"),
    NOT_EXIST_EMAIL(400, "DMWHD", "존재하지 않는 이메일"),
    EXIST_PHONE_NUMBER(400, "IQPEX", "이미 존재하는 전화번호"),
    NOT_EXIST_PHONE_NUMBER(400, "OKEN7", "가입되지 않은 전화번호"),
    NOT_EXIST_USER(400, "J6JE2", "등록 된 회원이 아님"),
    FAILURE_SMS_SIGNATURE_CREATION(400, "65A3Y", "SMS Signature 생성 실패"),
    NOT_EXIST_S3_FILE(400, "VFL42", "S3 해당 파일 객체가 존재하지 않음"),
    CONFIRMATION_REQUIRED_TERMS(400, "C01IE", "필수 약관을 확인"),
    OVER_VERIFICATION_CODE_PER_DAY(400, "PYRW2", "하루 인증번호 발송 횟수 초과"),
    ALREADY_DELETED_FILE(400, "2DYSM", "이미 삭제된 파일"),
    REQUIRE_FILE(400, "8L8ZZ", "파일 필수"),
    WITHOUT_FILE_EXTENSION(400, "Y1L0W", "확장자가 없는 파일"),
    BLOCKING_USER(401, "HI2FB", "서비스 이용 불가능 사용자"),
    CHECK_TEXT_LENGTH(400, "QC3P7", "텍스트 입력 조건 확인"),
    EXIST_TEXT(400, "N7L8U", "텍스트 존재"),
    REFRESH_TOKEN_REISSUE_AFTER_3_DAYS(400, "HO4HM", "리프레시 토큰 발급 3일 경과 후, 재발급 가능"),
    MAX_UPLOAD_SIZE_EXCEEDED(400, "K9KN1", "최대 업로드 사이즈 초과"),
    MAX_ADD_SIZE_EXCEEDED(400, "JH3S6", "최대 등록 가능한 작물 개수 초과"),
    MAX_PROFILE_LINK_ADD_SIZE_EXCEEDED(400, "P9M4Y", "최대 등록 가능한 작물 개수 초과"),
    NOT_EXISTS_CROP(400, "CR7E9", "존재하지 않는 작물입니다."),
    NOT_EXISTS_FARMER_PROFILE(400, "C0FM9", "존재하지 않는 농부 프로필입니다."),
    NOT_EXISTS_DESIGNER_PROFILE(400, "C0FM9", "존재하지 않는 디자이너 프로필입니다."),
    NOT_EXISTS_FARMER_PROFILE_LINK(400, "F7UL0", "존재하지 않는 농부 링크입니다."),
    NOT_EXISTS_FARMER(400, "F7M1R", "존재하지 않는 농부 정보입니다."),
    NOT_EXISTS_DESIGNER(400, "D2E5R", "존재하지 않는 디자이너 정봉보입니다."),
    NOT_EXISTS_JOB_TITLE(400, "J8M7B", "존재하지 않는 직업 유형입니다.");


    private int httpStatusCode;
    private String code;
    private String message;

    Status(int httpStatusCode, String code, String message) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.message = message;
    }

    public int httpStatusCode() {
        return this.httpStatusCode;
    }

    public String code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}