package io.hhplus.tdd.order;

public record PointOrder(
        long id,
        long point
//        String orderKey
) {

    public static PointOrder empty(long id) {
        return new PointOrder(id, 0);
    }

}
