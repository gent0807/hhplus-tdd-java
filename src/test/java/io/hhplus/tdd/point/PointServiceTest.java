package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
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


}
