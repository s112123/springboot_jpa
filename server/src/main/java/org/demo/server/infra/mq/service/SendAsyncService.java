package org.demo.server.infra.mq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.config.MQConfig;
import org.demo.server.infra.mq.constant.MessageType;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.module.follow.service.FollowFinder;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendAsyncService {

    private final MessageService messageService;
    private final RabbitTemplate rabbitTemplate;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;
    private final FollowFinder followFinder;

    /**
     * 새글  메세지를 RDB 에 저장하고 RabbitMQ 로 메세지 알림
     * MessageService 의 save() 에 @Transactional 이 있고 RabbitTemplate 는 트랜잭션 외부에서 동작된다
     * 그러므로 @Transactional 을 붙이지 않아도 된다
     *
     * @param publisherId 메세지를 발송하는 회원 ID (= 글 작성자)
     * @param reviewId 등록된 새 글 ID
     */
    @Async("sendPostTaskExecutor")
    public void publishPostAndSendMessage(Long publisherId, Long reviewId) {
        log.info("publishPostAndSendMessage: {}", Thread.currentThread().getName());

        // 새 글을 등록한 회원 조회
        Member publisher = memberFinder.getMemberById(publisherId);
        // 새 글 조회
        Review review = reviewFinder.getReviewById(reviewId);
        // 팔로워 조회
        List<MemberDetails> followers = followFinder.getFollowers(publisherId);

        // 비동기로 메세지 저장 및 알림 처리
        for (MemberDetails follower : followers) {
            // 메세지
            String message = publisher.getUsername() + "님이 [" + review.getTitle() + "] 리뷰를 등록하였습니다";
            // URL
            String url = "/review/view?review_id=" + review.getReviewId();
            // 메세지 저장 → RDB
            MessageDetails savedMessage =
                    messageService.save(MessageType.POST, publisherId, follower.getMemberId(), message, url);
            // 메세지 전송
            rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_NOTICE, savedMessage);
        }
    }

    /**
     * 공지 메세지를 RDB 에 저장하고 RabbitMQ 로 메세지 알림
     * MessageService 의 save() 에 @Transactional 이 있고 RabbitTemplate 는 트랜잭션 외부에서 동작된다
     * 그러므로 @Transactional 을 붙이지 않아도 된다
     *
     * @param publisherId 공지를 작성한 작성자 ID
     */
    @Async("sendNoticeTaskExecutor")
    public void publishNoticeAndSendMessage(Long publisherId, Long noticeId) {
        log.info("publishNoticeAndSendMessage: {}", Thread.currentThread().getName());

        // 전체 회원 조회
        List<Member> consumers = memberFinder.getAllMembers();

        // 비동기로 메세지 저장 및 알림 처리
        for (Member consumer : consumers) {
            if (consumer.getMemberId() != publisherId) {
                // 메세지
                String message = "새 공지가 있습니다";
                // URL
                String url = "/my/notice?noticeId=" + noticeId;
                // 메세지 저장 → RDB
                MessageDetails savedMessage =
                        messageService.save(MessageType.NOTICE, publisherId, consumer.getMemberId(), message, url);
                // 메세지 전송
                rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, MQConfig.ROUTING_NOTICE, savedMessage);
            }
        }
    }
}
