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
        return couponOps.getFirst();
    }

    @Transactional
    public void provide(Long accountId, UUID couponId) {
        if (Objects.isNull(couponId)) {
            RLock lock = redissonClient.getLock("coupon");

            try {
                lock.tryLock(60, 5, TimeUnit.SECONDS);
                insertCouponIdsInRedis(couponOps);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }

            couponId = couponOps.getFirst();
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow();

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
