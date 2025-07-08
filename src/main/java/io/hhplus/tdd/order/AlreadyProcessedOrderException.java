package io.hhplus.tdd.order;

public class AlreadyProcessedOrderException extends RuntimeException {

    public AlreadyProcessedOrderException(Throwable cause) {
        super(cause);
        setStackTrace(cause.getStackTrace());
    }

    public AlreadyProcessedOrderException(String message, Throwable cause) {
        super(message, cause);
        setStackTrace(cause.getStackTrace());
    }

    public AlreadyProcessedOrderException(String message) {
        super(message);
    }
}
