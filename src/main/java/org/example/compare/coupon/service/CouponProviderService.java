package org.example.compare.coupon.service;

import lombok.RequiredArgsConstructor;
import org.example.compare.config.RabbitmqConfig;
import org.example.compare.coupon.dto.CouponRequestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponProviderService {

    private final RabbitTemplate template;

    public void request(CouponRequestDto request) {
        this.template.convertAndSend(RabbitmqConfig.QUEUE_NAME, request);
    }
}
