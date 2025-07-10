package io.hhplus.tdd.point;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class PointException extends RuntimeException {

    private final PointErrorType errorCode;

    public PointException(PointErrorType errorType) {
        super(errorType.getLog());
        this.errorCode = errorType;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public String getHttpStatusValue() {
        return errorCode.getHttpStatus().value() + "";
    }


    public String getErrorName() {
        return errorCode.name();
    }

    public String getLog() {
        return errorCode.getLog();
    }


    public enum PointErrorType {
        CHARGE_AMOUNT_TOO_LOW(HttpStatus.BAD_REQUEST, "충전 금액은 0보다 커야 합니다."),
        USE_AMOUNT_TOO_LOW(HttpStatus.BAD_REQUEST, "사용 금액은 0보다 커야 합니다."),
        USE_AMOUNT_TOO_MUCH(HttpStatus.BAD_REQUEST, "사용 금액이 잔액을 초과했습니다.");

        @Getter
        private final HttpStatus httpStatus;

        @Getter
        private final String log;

        PointErrorType(HttpStatus status, String log) {
            this.httpStatus = status;
            this.log = log;
        }

    }
}
