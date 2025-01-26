package org.example.compare.coupon.service;

import org.example.compare.config.RabbitmqConfig;
import org.example.compare.coupon.dto.CouponRequestDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class CouponConsumerService {

    @RabbitListener(queues = RabbitmqConfig.QUEUE_NAME, concurrency = "5-10")
    public void receive(CouponRequestDto request) {
        System.out.println(request.accountId());
    }
}
