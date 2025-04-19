package org.demo.server.module.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.service.publisher.MessagePublisher;
import org.demo.server.module.chat.dto.Message;
import org.demo.server.module.chat.dto.details.ChatMemberDetails;
import org.demo.server.module.chat.dto.details.ChatMessageDetails;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatMessage;
import org.demo.server.module.chat.entity.ChatParticipant;
import org.demo.server.module.chat.entity.ChatRoom;
import org.demo.server.module.chat.repository.ChatMessageReadRepository;
import org.demo.server.module.chat.repository.ChatMessageRepository;
import org.demo.server.module.chat.repository.ChatParticipantRepository;
import org.demo.server.module.chat.repository.ChatRoomRepository;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.follow.repository.FollowRepository;
import org.demo.server.module.follow.service.FollowFinder;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberFinder memberFinder;
    private final FollowRepository followRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadRepository chatMessageReadRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessagePublisher messagePublisher;

    public ChatService(
            @Qualifier("redisTemplate03") RedisTemplate<String, String> redisTemplate,
            MemberFinder memberFinder,
            FollowRepository followRepository,
            ChatRoomRepository chatRoomRepository,
            ChatParticipantRepository chatParticipantRepository,
            ChatMessageRepository chatMessageRepository,
            ChatMessageReadRepository chatMessageReadRepository,
            SimpMessagingTemplate messagingTemplate, MessagePublisher messagePublisher
    ) {
        this.redisTemplate = redisTemplate;
        this.memberFinder = memberFinder;
        this.followRepository = followRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatMessageReadRepository = chatMessageReadRepository;
        this.messagingTemplate = messagingTemplate;
        this.messagePublisher = messagePublisher;
    }

    /**
     * 채팅방 참여하고 채팅방 메세지 목록 가져오기
     *
     * @param request 채팅방 참여자의 식별자
     * @Return 채팅 메시지 목록
     */
    @Transactional
    public List<ChatMessageDetails> joinChatRoom(ChatRoomRequest request) {
        // 회원 엔티티
        Member from = memberFinder.getMemberById(request.getFrom());
        Member to = memberFinder.getMemberById(request.getTo());
        // 채팅방 생성
        ChatRoom chatRoom = createAndJoinChatRoom(from, to);

        // 채팅방 참여 정보 저장
        String redisKey = "chat:member:" + from.getMemberId();
        redisTemplate.opsForValue().set(redisKey, String.valueOf(chatRoom.getChatRoomId()));

        // 채팅방의 메세지 목록
        return chatMessageRepository.findByChatRoom_ChatRoomId(chatRoom.getChatRoomId()).stream()
                .map(chatMessage -> new ChatMessageDetails(chatMessage))
                .collect(Collectors.toList());
    }

    /**
     * 채팅 대상자에게 메세지 전송
     *
     * @param message 전송할 메세지 정보
     */
    @Transactional
    public void sendMessage(Message message) {
        Member from = memberFinder.getMemberById(Long.valueOf(message.getFrom()));
        Member to = memberFinder.getMemberById(Long.valueOf(message.getTo()));

        // 채팅방
        ChatRoom chatRoom = createAndJoinChatRoom(from, to);

        // 메세지를 데이터베이스에 저장
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.addChatRoom(chatRoom);
        chatMessage.addMember(from);
        chatMessage.setMessage(from.getMemberId(), to.getMemberId(), message.getMessage());

        // 채팅 대상자가 채팅에 참여하고 하고 있는 방 번호
        String redisKey = "chat:member:" + to.getMemberId();
        String joinedChatRoomIdWithTo = redisTemplate.opsForValue().get(redisKey);
        if (joinedChatRoomIdWithTo != null) {
            // 메세지를 받는 채팅 대상자가 동일한 방에 접속 중이면 채팅 대상자에게 메세지 전송
            if (Long.valueOf(joinedChatRoomIdWithTo).equals(chatRoom.getChatRoomId())) {
                // 채팅 대상자에게 실시간 메세지 전송
                messagingTemplate.convertAndSendToUser(message.getTo(), "/chat/subscribe", message);
                return;
            }
        }
        // 채팅 대상자가 동일한 채팅방이 아닌 다른 페이지에 있으면 채팅 메세지 알림 전송
        messagePublisher.publishChat(from.getMemberId(), to.getMemberId());
    }

    /**
     * 동일한 채팅 대상자들의 채팅방이 존재하는지 여부
     *
     * @param from 채팅 메세지를 보낸 회원
     * @param to 채팅 메시지를 받을 회원
     * @return 이미 채팅방이 존재하면 true, 존재하지 않으면 false
     */
    private Set<Long> findChatRoom(Member from, Member to) {
        // 매개변수 from 의 인수로 들어온 회원이 참여하고 있는 채팅방의 식별자 목록
        Set<Long> fromChatRoomIds = getChatRoomIds(from.getMemberId());
        // 매개변수 to 의 인수로 들어온 회원이 참여하고 있는 채팅방의 식별자 목록
        Set<Long> toChatRoomIds = getChatRoomIds(to.getMemberId());
        // 두 Set 집합의 교집합 처리
        fromChatRoomIds.retainAll(toChatRoomIds);
        return fromChatRoomIds;
    }

    /**
     * 회원이 참여하고 있는 채팅방의 식별자 (chat_room_id) 목록 반환
     *
     * @param memberId 회원 식별자
     * @return 회원이 참여하고 있는 채팅방의 식별자 (chat_room_id) 목록
     */
    private Set<Long> getChatRoomIds(Long memberId) {
        return chatParticipantRepository.findByMember_MemberId(memberId).stream()
                .map(fromParticipant -> fromParticipant.getChatRoom().getChatRoomId())
                .collect(Collectors.toSet());
    }

    /**
     * 1:1 채팅방을 만들고 참여하기
     *
     * @param from 채팅방에 참여하는 회원의 식별자
     * @param to 채팅방에 참여하는 회원의 식별자
     * @return 생성된 채팅창 정보
     */
    private ChatRoom createAndJoinChatRoom(Member from, Member to) {
        // 유효성 검사 → 채팅방 존재 여부 확인
        if (!findChatRoom(from, to).isEmpty()) {
            Long chatRoomId = getChatRoomId(from, to);
            return chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new NoSuchElementException());
        }

        // 채팅방 만들기
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(UUID.randomUUID().toString())
                .build();

        // 채팅방 참여
        joinChatRoom(chatRoom, from);
        joinChatRoom(chatRoom, to);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return savedChatRoom;
    }

    /**
     * 채팅방 참여
     *
     * @param chatRoom 참여할 채팅방
     * @param member 참여할 회원
     */
    private void joinChatRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant participant = new ChatParticipant();
        participant.addChatRoom(chatRoom);
        participant.addMember(member);
    }

    /**
     * 채팅방 나가기
     *
     * @param memberId 채팅방에서 나가는 회원 ID
     */
    public void exitChatRoom(Long memberId) {
        String redisKey = "chat:member:" + memberId;
        redisTemplate.delete(redisKey);
    }

    /**
     * 채팅 대상자 목록 (= 내가 구독한 사람 목록)
     *
     * @param memberId 회원의 ID
     * @return 채팅 대상자 목록
     */
    public List<ChatMemberDetails> getChatMembers(Long memberId) {
        List<ChatMemberDetails> chatMembers = new ArrayList<>();

        // 채팅 대상자 목록
        List<Follow> follows = followRepository.findByFollower_MemberId(memberId);
        for (Follow follow : follows) {
            // 팔로우가 보낸 메세지 중 읽지 않은 메세지가 있는지 여부
            // 만약, 회원 ID 가 1인 회원이 읽지 않은 메세지를 조회하려면 receiverId 에 1, senderId 에 다른 회원 ID 를 넣는다
            boolean hasUnreadMessages = chatMessageReadRepository
                        .existsBySenderIdAndReceiverIdAndReadFalse(follow.getFollowed().getMemberId(), memberId);
            ChatMemberDetails chatMemberDetails = new ChatMemberDetails(follow, hasUnreadMessages);
            chatMembers.add(chatMemberDetails);
        }

        return chatMembers;
    }

    /**
     * 참여하고 있는 채팅방 ID
     *
     * @param from 메세지를 전송하는 회원
     * @param to 메세지를 받는 회원
     * @return 채팅방 ID
     */
    public Long getChatRoomId(Member from, Member to) {
        return findChatRoom(from, to).stream()
                .findFirst()
                .get();
    }

    /**
     * 모두 읽음 처리
     *
     * @param senderId 메세지를 보낸 사람
     * @param receiverId 메세지를 받은 사람
     */
    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        chatMessageReadRepository.markAsRead(senderId, receiverId);
    }
}
