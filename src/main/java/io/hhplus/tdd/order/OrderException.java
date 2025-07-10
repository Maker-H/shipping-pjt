package io.hhplus.tdd.order;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class OrderException extends RuntimeException {

    private final OrderErrorType errorCode;

    public OrderException(OrderErrorType errorType) {
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

    public enum OrderErrorType {

        ALREADY_ORDERED(HttpStatus.CONFLICT, "이미 주문이 처리되었습니다."),
        TOO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "요청량이 너무 많습니다");

        @Getter
        private final HttpStatus httpStatus;

        @Getter
        private final String log;

        OrderErrorType(HttpStatus httpStatus, String log) {
            this.httpStatus = httpStatus;
            this.log = log;
        }

    }
}
