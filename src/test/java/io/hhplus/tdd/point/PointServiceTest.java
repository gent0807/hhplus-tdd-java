package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;


import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointServiceTest {

    // 의존 상황의 대역 생성
    UserPointTable userPointTable = mock(UserPointTable.class);

    // 테스트 상황 설정
    PointService pointService = new PointService(userPointTable);

    @DisplayName("임의의 long type data를 id에 input => 해당 id에 맞는 UserPoint return")
    @Test
    void 임의의_long_type_값을_id에_입력하면_해당_id에_맞는_UserPoint가_return() {

        long id = new Random().nextLong();

        long point = 100L;

        long time = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(id, point, time);

        // 의존 상황 설정, 의존 대상 실행
        when(userPointTable.selectById(id))
                .thenReturn(userPoint);

        // 결과 확인
        assertEquals(userPoint, pointService.selectById(id));

        Mockito.verify(userPointTable).selectById(id);

        // 다른 케이스도 하나 더 검사

        id = new Random().nextLong();

        point = 1_000_000L;

        userPoint = new UserPoint(id, point, time);

        when(userPointTable.selectById(id))
                .thenReturn(userPoint);

        assertEquals(userPoint, pointService.selectById(id));

        Mockito.verify(userPointTable).selectById(id);

    }


}
