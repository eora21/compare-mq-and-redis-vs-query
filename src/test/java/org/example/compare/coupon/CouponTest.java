package org.example.compare.coupon;

import org.assertj.core.api.Assertions;
import org.example.compare.coupon.controller.CouponController;
import org.example.compare.coupon.dto.CouponRequestDto;
import org.example.compare.coupon.repository.CouponRepository;
import org.example.compare.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.StopWatch;

import java.util.concurrent.CountDownLatch;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@Sql(scripts = "classpath:ddl.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CouponTest {
    private static final int TRY_COUNT = 1_000;

    @Autowired
    private CouponController couponController;

    @MockitoSpyBean
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("RabbitMQ와 Redis, Redisson을 사용한 테스트 시간 측정")
    void couponRabbitMqRedisTest() throws Exception {

        // given
        StopWatch stopWatch = new StopWatch();
        CountDownLatch countDownLatch = new CountDownLatch(TRY_COUNT);

        doAnswer(invocation -> {
            invocation.callRealMethod();
            countDownLatch.countDown();
            return null;
        }).when(couponService).provide(anyLong(), any());

        // when
        stopWatch.start();
        LongStream.rangeClosed(1, TRY_COUNT)
                .parallel()
                .mapToObj(CouponRequestDto::new)
                .forEach(couponController::requestCouponUsingRabbitMqAndRedis);

        countDownLatch.await();
        stopWatch.stop();

        // then
        Assertions.assertThat(couponRepository.findAllByAccountIdIsNull()).isEmpty();
        System.out.println("모든 요청을 처리하는 데 걸린 시간 = " + stopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    @DisplayName("Redis, Redisson을 사용한 테스트 시간 측정")
    void couponRedisTest() throws Exception {

        // given
        StopWatch stopWatch = new StopWatch();
        CountDownLatch countDownLatch = new CountDownLatch(TRY_COUNT);

        doAnswer(invocation -> {
            invocation.callRealMethod();
            countDownLatch.countDown();
            return null;
        }).when(couponService).provide(anyLong(), any());

        // when
        stopWatch.start();
        LongStream.rangeClosed(1, TRY_COUNT)
                .parallel()
                .mapToObj(CouponRequestDto::new)
                .forEach(couponController::requestCouponUsingRedis);

        countDownLatch.await();
        stopWatch.stop();

        // then
        Assertions.assertThat(couponRepository.findAllByAccountIdIsNull()).isEmpty();
        System.out.println("모든 요청을 처리하는 데 걸린 시간 = " + stopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    @DisplayName("SKIP LOCKED를 사용한 테스트 시간 측정")
    void couponSkipLockedTest() throws Exception {

        // given
        StopWatch stopWatch = new StopWatch();
        CountDownLatch countDownLatch = new CountDownLatch(TRY_COUNT);

        doAnswer(invocation -> {
            invocation.callRealMethod();
            countDownLatch.countDown();
            return null;
        }).when(couponService).provide(anyLong());

        // when
        stopWatch.start();
        LongStream.rangeClosed(1, TRY_COUNT)
                .parallel()
                .mapToObj(CouponRequestDto::new)
                .forEach(couponController::requestCouponWithSkipLocked);

        countDownLatch.await();
        stopWatch.stop();

        // then
        Assertions.assertThat(couponRepository.findAllByAccountIdIsNull()).isEmpty();
        System.out.println("모든 요청을 처리하는 데 걸린 시간 = " + stopWatch.getTotalTimeMillis() + "ms");
    }
}
