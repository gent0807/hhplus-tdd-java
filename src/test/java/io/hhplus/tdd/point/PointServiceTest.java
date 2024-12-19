package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class PointServiceTest {

    // 의존 상황의 대역 생성
    UserPointTable userPointTable = mock(UserPointTable.class);

    // 의존 상황의 대역 생성
    PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);

    // 테스트 상황 설정
    PointService pointService = new PointService(userPointTable, pointHistoryTable);


    @DisplayName("임의의 long type data를 id에 input => 해당 id에 맞는 UserPoint return")
    @Test
    void 임의의_long_type_값을_id에_입력하면_해당_id에_맞는_UserPoint가_return() {

        assertSelectUserPoint(new Random().nextLong(), 100L, System.currentTimeMillis());

        assertSelectUserPoint( new Random().nextLong(), 1_000_000L, System.currentTimeMillis());

        assertSelectUserPoint(Long.MAX_VALUE, 100L, System.currentTimeMillis());

        assertSelectUserPoint(Long.MIN_VALUE, -1L, System.currentTimeMillis());

    }

    private void assertSelectUserPoint(long id, long point, long updateMillis) {
        UserPoint userPoint = new UserPoint(id, point, updateMillis);

        // 의존 상황 설정, 의존 대상 실행
        when(userPointTable.selectById(id))
                .thenReturn(userPoint);

        // 결과 확인
        assertThat(pointService.selectById(id))
                .extracting("id", "point", "updateMillis")
                        .containsExactly(id, point, updateMillis);

        Mockito.verify(userPointTable).selectById(id);
    }

    @DisplayName("임의의 long type data를 user id에 input => 해당 id에 맞는 PointHistory return")
    @Test
    void 임의의_long_type_값을_user_id에_입력하면_해당_id에_맞는_PointHistory가_return() {

        // given
        long userId = 100L;

        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(List.of(
                        new PointHistory(1L, userId, 2000L, TransactionType.CHARGE,100L ),
                        new PointHistory(2L, userId, 1000L, TransactionType.USE, 200L),
                        new PointHistory(3L, userId, 3000L, TransactionType.CHARGE, 300L)
                ));

        // when // then
        assertThat(pointService.selectAllById(userId)).hasSize(3)
                .extracting("id", "userId", "amount", "type", "updateMillis")
                .containsExactly(
                        tuple(1L, userId, 2000L, TransactionType.CHARGE,100L ),
                        tuple(2L, userId, 1000L, TransactionType.USE, 200L),
                        tuple(3L, userId, 3000L, TransactionType.CHARGE, 300L)
                );

        Mockito.verify(pointHistoryTable).selectAllByUserId(userId);


    }


    @DisplayName("포인트_충전량이_포인트_최대치보다_큰_경우_익셉션_발생")
    @Test
    void 포인트_충전량이_포인트_최대치보다_큰_경우_익셉션_발생() {

        long userId = 100L;
        long amount = pointService.MAX_POINT + 1;

        assertThatIllegalArgumentException()
                .isThrownBy(()->pointService.addPoint(userId, amount));


    }

    @DisplayName("포인트_충전량이_1보다_작은_경우_익셉션_발생")
    @Test
    void 포인트_충전량이_1보다_작은_경우_익셉션_발생() {

        long userId = 100L;
        long amount = 0;

        assertThatIllegalArgumentException()
                .isThrownBy(()->pointService.addPoint(userId, amount));
    }

    @DisplayName("입력된_id가_현재_보유한_포인트와_포인트_충전량의_합산이_포인트의_최대치보다_큰_경우_익셉션_발생")
    @Test
    void 입력된_id가_현재_보유한_포인트와_포인트_충전량의_합산이_포인트의_최대치보다_큰_경우_익셉션_발생() {

        long userId = 1L;
        long point = 1L;
        long amount = pointService.MAX_POINT;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        assertThatIllegalArgumentException()
            .isThrownBy(()->pointService.addPoint(userId, amount));

        Mockito.verify(userPointTable).selectById(userId);

    }

    @DisplayName("포인트_충전량의_모든_조건을_만족하면_포인트를_충전한다")
    @Test
    void 포인트_충전량의_모든_조건을_만족하면_포인트를_충전한다() {

        long userId = 1L;
        long nowPoint = 1L;
        long amount = pointService.MAX_POINT - 2L;
        long changePoint = nowPoint + amount;
        long updateMillis = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, nowPoint, updateMillis);
        UserPoint changeUserPoint = new UserPoint(userId, changePoint, updateMillis);

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        when(userPointTable.insertOrUpdate(userId, changePoint))
                .thenReturn(changeUserPoint);

        assertThat(pointService.addPoint(userId, amount))
                .isEqualTo(changeUserPoint);

        Mockito.verify(userPointTable).selectById(userId);

        Mockito.verify(userPointTable).insertOrUpdate(userId, changePoint);
    }

    @DisplayName("포인트_충전량의_모든_조건을_만족하면_포인트를_충전하고_내역을_저장한다")
    @Test
    void 포인트_충전량의_모든_조건을_만족하면_포인트를_충전하고_내역을_저장한다() {
        long userId = 1L;
        long nowPoint = 1L;
        long amount = pointService.MAX_POINT - 2L;
        long changePoint = nowPoint + amount;
        TransactionType type = TransactionType.CHARGE;
        long updateMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(userId, nowPoint, updateMillis);
        UserPoint changeUserPoint = new UserPoint(userId, changePoint, updateMillis);
        PointHistory pointHistory = new PointHistory(1L, userId, amount, type, updateMillis);

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        when(userPointTable.insertOrUpdate(userId, changePoint))
                .thenReturn(changeUserPoint);

        when(pointHistoryTable.insert(userId, amount, type, updateMillis))
                .thenReturn(pointHistory);

        assertThat(pointService.addPoint(userId, amount))
                .isEqualTo(changeUserPoint);

        Mockito.verify(userPointTable).selectById(userId);
        Mockito.verify(userPointTable).insertOrUpdate(userId, changePoint);
        Mockito.verify(pointHistoryTable).insert(userId, amount, type, updateMillis);
    }

    @DisplayName("포인트_사용량이_1보다_작은_경우_익셉션_발생")
    @Test
    void 포인트_사용량이_1보다_작은_경우_익셉션_발생() {
        long userId = 100L;
        long amount = 0;

        assertThatIllegalArgumentException()
                .isThrownBy(()->pointService.usePoint(userId, amount));
    }

    @Test
    void 포인트_사용량이_최대치보다_큰_경우_익셉션_발생() {
        long userId = 100L;
        long amount = pointService.MAX_POINT + 1;

        assertThatIllegalArgumentException()
                .isThrownBy(()->pointService.usePoint(userId, amount));

    }

    @DisplayName("입력된_id가_현재_보유한_포인트와_포인트_사용량의_감산_결과가_0보다_작은_경우_익셉션_발생")
    @Test
    void 입력된_id가_현재_보유한_포인트와_포인트_사용량의_감산_결과가_0보다_작은_경우_익셉션_발생() {

        long userId = 1L;
        long point = 1L;
        long amount = 2L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        assertThatIllegalArgumentException()
                .isThrownBy(()->pointService.usePoint(userId, amount));

        Mockito.verify(userPointTable).selectById(userId);

    }

    @DisplayName("포인트_감산량의_모든_조건을_만족하면_포인트를_사용한다")
    @Test
    void 포인트_충전량의_모든_조건을_만족하면_포인트를_사용한다() {
        long userId = 1L;
        long point = pointService.MAX_POINT;
        long amount = pointService.MAX_POINT - 1L;
        long changePoint = point - amount;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint changeUserPoint = new UserPoint(userId, point-amount, System.currentTimeMillis());

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        when(userPointTable.insertOrUpdate(userId, changePoint))
                .thenReturn(changeUserPoint);

        assertThat(pointService.usePoint(userId, amount))
                .isEqualTo(changeUserPoint);

        Mockito.verify(userPointTable).selectById(userId);
        Mockito.verify(userPointTable).insertOrUpdate(userId, changePoint);


    }

    @DisplayName("포인트_사용량의_모든_조건을_만족하면_포인트를_사용하고_내역을_저장한다")
    @Test
    void 포인트_사용량의_모든_조건을_만족하면_포인트를_사용하고_내역을_저장한다() {
        long userId = 1L;
        long nowPoint = pointService.MAX_POINT;
        long amount = pointService.MAX_POINT - 1L;
        long changePoint = nowPoint - amount;
        TransactionType type = TransactionType.USE;
        long updateMillis = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(userId, nowPoint, updateMillis);
        UserPoint changeUserPoint = new UserPoint(userId, changePoint, updateMillis);
        PointHistory pointHistory = new PointHistory(1L, userId, amount, type, updateMillis);

        when(userPointTable.selectById(userId))
                .thenReturn(userPoint);

        when(userPointTable.insertOrUpdate(userId, changePoint))
                .thenReturn(changeUserPoint);

        when(pointHistoryTable.insert(userId, amount, type, updateMillis))
                .thenReturn(pointHistory);

        assertThat(pointService.usePoint(userId, amount))
                .isEqualTo(changeUserPoint);

        Mockito.verify(userPointTable).selectById(userId);
        Mockito.verify(userPointTable).insertOrUpdate(userId, changePoint);
        Mockito.verify(pointHistoryTable).insert(userId, amount, type, updateMillis);
    }
}
