package org.example.compare.coupon.controller;

import lombok.RequiredArgsConstructor;
import org.example.compare.config.RabbitmqConfig;
import org.example.compare.coupon.dto.CouponRequestDto;
import org.example.compare.coupon.service.CouponService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final RabbitTemplate template;
    private final CouponService couponService;

    @PostMapping
    public void requestCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        this.template.convertAndSend(RabbitmqConfig.QUEUE_NAME, couponRequestDto.accountId());
    }

    @RabbitListener(queues = RabbitmqConfig.QUEUE_NAME, concurrency = "5-10")
    public void provideCoupon(Long accountId) {
        UUID couponId = couponService.getCouponId();
        couponService.provide(accountId, couponId);
    }
}
