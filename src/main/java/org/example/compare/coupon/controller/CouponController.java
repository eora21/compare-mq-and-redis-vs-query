package org.example.compare.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.example.compare.coupon.dto.CouponRequestDto;
import org.example.compare.coupon.service.CouponProviderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponProviderService couponProviderService;

    @PostMapping
    public void wantCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        couponProviderService.request(couponRequestDto);
    }
}
