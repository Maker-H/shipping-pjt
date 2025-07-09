package io.hhplus.tdd;

import io.hhplus.tdd.order.OrderException;
import io.hhplus.tdd.point.PointException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(value = PointException.class)
    public ResponseEntity<ErrorResponse> handlePointException(PointException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value() + "", e.getMessage()));
    }

    @ExceptionHandler(value = OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(OrderException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value() + "", e.getMessage()));
    }

}
