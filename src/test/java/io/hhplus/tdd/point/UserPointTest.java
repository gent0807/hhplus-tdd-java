package io.hhplus.tdd.point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserPointTest {

    @Test
    void id에_long_type_1을_입력했을_때_UserPoint가_null이면_안_된다() {
        Assertions.assertThat(UserPoint.empty(1L)).isNotNull();
    }

    @Test
    void id에_long_type_최소치를_입력했을_때_UserPoint가_null이면_안_된다() {
        Assertions.assertThat(UserPoint.empty(Long.MIN_VALUE)).isNotNull();
    }

    @Test
    void id에_long_type_최대치를_입력했을_때_UserPoint가_null이면_안_된다() {
        Assertions.assertThat(UserPoint.empty(Long.MAX_VALUE)).isNotNull();
    }
}
