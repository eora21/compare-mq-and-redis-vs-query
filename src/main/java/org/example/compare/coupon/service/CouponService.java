package org.example.compare.coupon.service;

import org.example.compare.coupon.entity.Coupon;
import org.example.compare.coupon.repository.CouponRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CouponService {
    private final RedissonClient redissonClient;
    private final CouponRepository couponRepository;
    private final BoundListOperations<String, UUID> couponOps;

    public CouponService(RedisTemplate<String, UUID> redisTemplate, RedissonClient redissonClient, CouponRepository couponRepository) {
        this.redissonClient = redissonClient;
        this.couponRepository = couponRepository;
        this.couponOps = redisTemplate.boundListOps("coupon");
    }

    public UUID getCouponId() {
        return couponOps.leftPop();
    }

    @Transactional
    public void provide(Long accountId, UUID couponId) {
        if (Objects.isNull(couponId)) {
            RLock lock = redissonClient.getLock("coupon_insert_lock");
            assert lock != null;

            try {
                lock.tryLock(60, 5, TimeUnit.SECONDS);
                insertCouponIdsInRedis(couponOps);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

            couponId = couponOps.leftPop();
        }

        if (Objects.isNull(couponId)) {
            return;
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 쿠폰id입니다."));

        coupon.setAccountId(accountId);
    }

    private void insertCouponIdsInRedis(BoundListOperations<String, UUID> couponOps) {
        List<Coupon> notProvidedCoupons = couponRepository.findAllByAccountIdIsNull();

        if (notProvidedCoupons.isEmpty()) {
            return;
        }

        UUID[] notProvideCouponIds = notProvidedCoupons.stream()
                .map(Coupon::getCouponCode)
                .toArray(UUID[]::new);

        couponOps.rightPushAll(notProvideCouponIds);
    }
}
