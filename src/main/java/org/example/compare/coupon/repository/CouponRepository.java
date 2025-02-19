package org.example.compare.coupon.repository;

import org.example.compare.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findAllByAccountIdIsNull();
}
