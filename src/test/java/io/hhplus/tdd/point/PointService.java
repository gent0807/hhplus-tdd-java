package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    long MAX_POINT = 5_000_000;

    public UserPoint selectById(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllById(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint addPoint(long userId, long amount) {

        if(amount > MAX_POINT) {
            throw new IllegalArgumentException();
        }

        if(amount < 1) {
            throw new IllegalArgumentException();
        }

        long point = userPointTable.selectById(userId).point();

        if(point + amount > MAX_POINT) {
            throw new IllegalArgumentException();
        }

        UserPoint userPoint = userPointTable.insertOrUpdate(userId, point + amount);

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis());

        return userPoint;

    }

}
