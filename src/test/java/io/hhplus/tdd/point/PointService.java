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



}
