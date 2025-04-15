package org.demo.server.infra.mq.service.publisher;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.config.MQConfig;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.service.MessageService;
import org.demo.server.module.follow.service.FollowFinder;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;
    private final MessageService messageService;
    private final FollowFinder followFinder;

    /**
     * 좋아요 알림 메세지 발송
     *
     * @param publisherId 메세지를 발송하는 회원 ID
     * @param reviewId 좋아요가 눌러진 리뷰 ID
     */
    @Transactional(readOnly = true)
    public void publishLike(Long publisherId, Long reviewId) {
        // 좋아요를 누른 회원 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 리뷰 조회
        Review findReview = reviewFinder.getReviewById(reviewId);
        // 메세지
        String message = publisher.getUsername() + "님이 [" + findReview.getTitle() + "] 리뷰에 좋아요를 눌렀습니다";
        // 메세지 저장 → RDB
        MessageDetails savedMessage =
                messageService.save(findReview.getMember().getMemberId(), message);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_LIKE, savedMessage);
    }

    /**
     * 구독 알림 메세지 전송
     *
     * @param publisherId 메세지를 발송하는 회원 ID (= 팔로워 ID)
     * @param consumerId 메세지를 받을 회원 ID (= 팔로우 ID)
     */
    @Transactional(readOnly = true)
    public void publishFollow(Long publisherId, Long consumerId) {
        // 구독을 누른 회원 (팔로워) 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 메세지
        String message = publisher.getUsername() + "님이 구독하였습니다";
        // 메세지 저장 → RDB
        MessageDetails savedMessage = messageService.save(consumerId, message);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_FOLLOW, savedMessage);
    }

    /**
     * 새 글 알림
     *
     * @param publisherId 메세지를 발송하는 회원 ID (= 글 작성자)
     * @param reviewId 등록된 새 글 ID
     */
    @Transactional(readOnly = true)
    public void publishPost(Long publisherId, Long reviewId) {
        // 새 글을 등록한 회원 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 새 글 조회
        Review review = reviewFinder.getReviewById(reviewId);
        // 팔로워 조회
        List<MemberDetails> followers = followFinder.getFollowers(publisherId);
        // 메세지 저장 → RDB
        for (MemberDetails follower : followers) {
            // 메세지
            String message = publisher.getUsername() + "님이 [" + review.getTitle() + "] 리뷰를 등록하였습니다";
            MessageDetails savedMessage = messageService.save(follower.getMemberId(), message);
            // 메세지 전송
            rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_POST, savedMessage);
        }
    }

    // 공지사항
    public void publishNotice(Long publisherId, Long consumerId) {
        // 메세지
        String message = "새 공지가 있습니다";
        MessageDetails savedMessage = messageService.save(consumerId, message);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_NOTICE, savedMessage);
    }

    // 채팅
}
