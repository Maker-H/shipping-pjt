package io.hhplus.tdd.point;

public class PointException extends RuntimeException{

    public PointException(Throwable cause) {
        super(cause);
        setStackTrace(cause.getStackTrace());
    }

    public PointException(String message, Throwable cause) {
        super(message, cause);
        setStackTrace(cause.getStackTrace());
    }

    public PointException(String message) {
        super(message);
    }
}
