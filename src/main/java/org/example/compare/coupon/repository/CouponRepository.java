package org.example.compare.coupon.repository;

import org.example.compare.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findAllByAccountIdIsNull();

    @Query(value = "SELECT * FROM coupons WHERE account_id IS NULL LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<Coupon> findProvidableOneWithSkipLocked();

    @Modifying
    @Query(value = "UPDATE coupons SET coupons.account_id = :accountId WHERE coupons.coupon_code = :couponCode", nativeQuery = true)
    void updateCouponAccountIdByCouponCode(@Param("couponCode") UUID couponCode, @Param("accountId") Long accountId);
}
