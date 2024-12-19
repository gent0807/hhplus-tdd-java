package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private ReentrantLock lock;

    long MAX_POINT = 5_000_000;

    public UserPoint selectById(long id) {

        return userPointTable.selectById(id);
    }

    public List<PointHistory> selectAllById(long userId) {

        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint addPoint(long userId, long amount) {

            lock = lockMap.computeIfAbsent(userId, key -> new ReentrantLock());

            if(amount > MAX_POINT) {
                throw new IllegalArgumentException();
            }

            if(amount < 1) {
                throw new IllegalArgumentException();
            }

            lock.lock();

            try {

                long point = userPointTable.selectById(userId).point();

                if(point + amount > MAX_POINT) {
                    throw new IllegalArgumentException();
                }



                UserPoint userPoint = userPointTable.insertOrUpdate(userId, point + amount);

                pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, userPoint.updateMillis());

                return userPoint;


            }finally {
                lock.unlock();
            }




    }

    public UserPoint usePoint(long userId, long amount) {

            lock = lockMap.computeIfAbsent(userId, key -> new ReentrantLock());


            if (amount > MAX_POINT) {
                throw new IllegalArgumentException();
            }

            if (amount < 1) {
                throw new IllegalArgumentException();
            }


            lock.lock();

            try {

                long point = userPointTable.selectById(userId).point();

                if (point - amount < 0) {
                    throw new IllegalArgumentException();
                }

                UserPoint userPoint = userPointTable.insertOrUpdate(userId, point - amount);

                pointHistoryTable.insert(userId, amount, TransactionType.USE, userPoint.updateMillis());

                return userPoint;
            }finally {
                lock.unlock();
            }
    }
}
