package org.demo.server.infra.mq.service.publisher;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.config.MQConfig;
import org.demo.server.infra.mq.constant.MessageType;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.service.MessageService;
import org.demo.server.infra.mq.service.SendAsyncService;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;
    private final FollowFinder followFinder;
    private final MessageService messageService;
    private final SendAsyncService sendAsyncService;

    /**
     * 좋아요 알림 메세지 발송
     *
     * @param publisherId 메세지를 발송하는 회원 ID
     * @param reviewId 좋아요가 눌러진 리뷰 ID
     */
    @Transactional
    public void publishLike(Long publisherId, Long reviewId) {
        // 좋아요를 누른 회원 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 리뷰 조회
        Review findReview = reviewFinder.getReviewById(reviewId);
        // 메세지
        String message = publisher.getUsername() + "님이 [" + findReview.getTitle() + "] 리뷰에 좋아요를 눌렀습니다";
        // URL
        String url = "/review/view?review_id=" + findReview.getReviewId();
        // 메세지 저장 → RDB
        MessageDetails savedMessage =
                messageService.save(MessageType.LIKE, findReview.getMember().getMemberId(), message, url);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_LIKE, savedMessage);
    }

    /**
     * 구독 알림 메세지 전송
     *
     * @param publisherId 메세지를 발송하는 회원 ID (= 팔로워 ID)
     * @param consumerId 메세지를 받을 회원 ID (= 팔로우 ID)
     */
    @Transactional
    public void publishFollow(Long publisherId, Long consumerId) {
        // 구독을 누른 회원 (팔로워) 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 메세지
        String message = publisher.getUsername() + "님이 구독하였습니다";
        // URL
        String url = "/my/profile";
        // 메세지 저장 → RDB
        MessageDetails savedMessage = messageService.save(MessageType.FOLLOW, consumerId, message, url);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_FOLLOW, savedMessage);
    }

    /**
     * 새 글 알림
     *
     * @param publisherId 메세지를 발송하는 회원 ID (= 글 작성자)
     * @param reviewId 등록된 새 글 ID
     */
    @Transactional
    public void publishPost(Long publisherId, Long reviewId) {
        // 비동기로 알림 메세지를 RDB 에 저장하고 새 글 알림 메세지를 팔로워에게 전송
        sendAsyncService.publishPostAndSendMessage(publisherId, reviewId);
    }

    /**
     * 전체 공지 알림
     *
     * @param publisherId 공지를 작성한 작성자 ID
     */
    @Transactional
    public void publishNotice(Long publisherId, Long noticeId) {
        // 비동기로 알림 메세지를 RDB 에 저장하고 공지 알림 메세지를 팔로워에게 전송
        sendAsyncService.publishNoticeAndSendMessage(publisherId, noticeId);
    }

    /**
     * 채팅 메세지 알림
     *
     * @param publisherId 채팅 메세지를 보낸 회원 ID
     * @param consumerId 채팅 메시지를 받는 회원 ID
     */
    @Transactional
    public void publishChat(Long publisherId, Long consumerId) {
        // 채팅 메세지를 보낸 회원
        Member publisher = memberFinder.getMemberById(publisherId);
        // 채팅 메세지를 받는 회원
        Member consumer = memberFinder.getMemberById(consumerId);

        // 메세지
        String message = publisher.getUsername() + "님이 메세지를 보냈습니다";
        // URL
        String url = "/my/chat";

        // 채팅 메세지를 보내는 회원을 구독한 회원 (팔로워) 의 ID 목록
        List<Long> followerIds = followFinder.getFollowers(publisherId).stream()
                .map(memberDetails -> memberDetails.getMemberId())
                .collect(Collectors.toList());

        // 채팅 메세지를 받는 회원이 채팅 메세지를 보내는 회원을 팔로워하고 있지 않다면 친구로 등록하지 않은 상태이다
        if (!followerIds.contains(consumerId)) {
            message = "[친구로 등록되지 않은 사용자] " + publisher.getUsername() + "님이 메세지를 보냈습니다";
            url = "/my/profile";
        }

        // 메세지 저장 → RDB
        MessageDetails savedMessage =
                messageService.save(MessageType.CHAT, consumer.getMemberId(), message, url);
        // 메세지 전송
        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_CHAT, savedMessage);
    }
}
