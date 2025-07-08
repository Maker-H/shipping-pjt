package io.hhplus.tdd.order;

public class OrderException extends RuntimeException {

    public OrderException(Throwable cause) {
        super(cause);
    }


    public OrderException(Message message) {
        super(message.name());
    }

    public enum Message {
        ALREADY_ORDERED,;

        public static Message fromString (String value) {
            for (Message message : values()) {
                if (message.name().equals(value)) {
                    return message;
                }
            }
            throw new IllegalArgumentException("No enum constant " + value);
        }
    }
}
