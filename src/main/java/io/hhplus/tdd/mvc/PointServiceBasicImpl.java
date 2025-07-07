package io.hhplus.tdd.mvc;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointException;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hhplus.tdd.point.TransactionType.CHARGE;

@Service
@RequiredArgsConstructor
public class PointServiceBasicImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    @Override
    public UserPoint charge(long id, long amount) {

        UserPoint existing = userPointTable.selectById(id);
        long totalPoint = existing.add(amount).point();

        pointHistoryTable.insert(id, amount, CHARGE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(id, totalPoint);
    }

    @Override
    public UserPoint get(long id) {
        return userPointTable.selectById(id);
    }
}
