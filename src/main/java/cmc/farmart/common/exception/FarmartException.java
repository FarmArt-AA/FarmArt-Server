package cmc.farmart.common.exception;

public class FarmartException extends RuntimeException {

    private int httpStatusCode;
    private String code;
    private String message;

    public FarmartException(Status status) {
        super(status.name());
        this.httpStatusCode = status.httpStatusCode();
        this.code = status.code();
        this.message = status.message();
    }

    public FarmartException(String message) {
        super(Status.BAD_REQUEST.name());
        this.httpStatusCode = Status.BAD_REQUEST.httpStatusCode();
        this.code = Status.BAD_REQUEST.code();
        this.message = message;
    }

    public FarmartException(String code, String message) {
        super(Status.BAD_REQUEST.name());
        this.httpStatusCode = Status.BAD_REQUEST.httpStatusCode();
        this.code = code;
        this.message = message;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
