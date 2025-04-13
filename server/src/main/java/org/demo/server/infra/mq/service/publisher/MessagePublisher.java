package org.demo.server.infra.mq.service.publisher;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.config.MQConfig;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.service.MessageService;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;
    private final MessageService messageService;

    // 좋아요 알림
    @Transactional(readOnly = true)
    public void publishLike(Long publisherId, Long reviewId) {
        // 좋아요를 누른 회원 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 리뷰 조회
        Review findReview = reviewFinder.getReviewById(reviewId);
        // 메세지
        java.lang.String message = publisher.getUsername() + "님이 [" + findReview.getTitle() + "] 리뷰에 좋아요를 눌렀습니다";
        // 메세지 저장 → RDB
        MessageDetails savedMessage =
                messageService.save(findReview.getMember().getMemberId(),message);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_LIKE, savedMessage);
    }

    // 팔로우 알림
    public void publishFollow() {
        java.lang.String message = "팔로우 하였습니다";
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_LIKE, message);
    }
}
