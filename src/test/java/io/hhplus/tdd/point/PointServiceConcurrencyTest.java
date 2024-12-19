package io.hhplus.tdd.point;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.RuntimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    void 동일한_id로_동시에_포인트_충전_혹은_포인트_사용_시_정확한_값으로_계산되어야_한다() throws InterruptedException {

        long userId = 1L;

        long addPoint = 10L;

        long usePoint = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Runnable addTask = () -> {
            pointService.addPoint(userId, addPoint);
        };

        Runnable useTask = () -> {
            pointService.usePoint(userId, usePoint);
        };

        for (int i = 0; i < 5; i++) {
            executor.submit(addTask);
            executor.submit(useTask);
        }

        executor.shutdown();

        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(pointService.selectById(userId))
                .extracting("id", "point")
                    .containsExactly(userId, 45L);

    }

    @Test
    void 서로_다른_id로_동시에_충전_혹은_사용_시_정확한_값으로_계산되야_한다() throws InterruptedException {
        // given

        ExecutorService executor = Executors.newFixedThreadPool(10);

        long addPoint = 100L;

        long usePoint = 1L;

        for(int i = 0; i <= 5; i++) {

            long userId = i;

            executor.submit(() -> {
                UserPoint userPoint = pointService.addPoint(userId, addPoint);
            });

            executor.submit(() -> {
                UserPoint userPoint = pointService.usePoint(userId, usePoint);
            });
        }

        executor.shutdown();

        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        for(int i = 0; i <= 5; i++) {
            assertThat(pointService.selectById(i))
                .extracting("id", "point")
                    .containsExactly((long)i, addPoint - usePoint);
        }

    }







}
