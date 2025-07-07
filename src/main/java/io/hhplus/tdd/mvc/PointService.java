package io.hhplus.tdd.mvc;

import io.hhplus.tdd.point.UserPoint;

public interface PointService {

    UserPoint charge(long id, long amount);

    UserPoint get(long id);

    UserPoint use(long id, long amount);
}
