package org.demo.server.module.chat.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.Message;
import org.demo.server.module.chat.dto.details.ChatMessageDetails;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatMessage;
import org.demo.server.module.chat.entity.ChatParticipant;
import org.demo.server.module.chat.entity.ChatRoom;
import org.demo.server.module.chat.repository.ChatMessageRepository;
import org.demo.server.module.chat.repository.ChatParticipantRepository;
import org.demo.server.module.chat.repository.ChatRoomRepository;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberFinder memberFinder;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
        chatMessage.addMessage(message.getMessage());

        // 채팅 대상자에게 메세지 전송
        messagingTemplate.convertAndSendToUser(message.getTo(), "/chat/subscribe", message);
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
            Long chatRoomId = findChatRoom(from, to).stream()
                    .findFirst()
                    .get();
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

        // 채팅방 참여 정보 저장
        return chatRoomRepository.save(chatRoom);
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
}
