package io.hhplus.tdd.order;

public record UserOrder(
        long id,
        long point
//        String orderKey
) {

    public static UserOrder empty(long id) {
        return new UserOrder(id, 0);
    }

}
