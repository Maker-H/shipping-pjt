package io.hhplus.tdd.mvc;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointService {

    UserPoint charge(long id, long amount);

    UserPoint get(long id);

    UserPoint use(long id, long amount);

    List<PointHistory> history(long id);
}
