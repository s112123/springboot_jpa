package org.demo.server.module.chat.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.Message;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatParticipant;
import org.demo.server.module.chat.entity.ChatRoom;
import org.demo.server.module.chat.repository.ChatParticipantRepository;
import org.demo.server.module.chat.repository.ChatRoomRepository;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberFinder memberFinder;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     *
     * @param request
     */
    @Transactional
    public void createChatRoom(ChatRoomRequest request) {
        // 회원 엔티티
        Member fromMember = memberFinder.getMemberById(request.getFrom());
        Member toMember = memberFinder.getMemberById(request.getTo());

        // 채팅 참여자 엔티티
        ChatParticipant fromParticipant = ChatParticipant.builder()
                .member(fromMember)
                .build();
        ChatParticipant toParticipant = ChatParticipant.builder()
                .member(toMember)
                .build();

        // 채팅방 엔티티
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.addChatParticipant(fromParticipant);
        chatRoom.addChatParticipant(toParticipant);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 채팅 대상자에게 메세지 전송
     *
     * @param message 전송할 메세지 정보
     */
    @Transactional
    public void sendMessage(Message message) {
        // 메세지를 데이터베이스에 저장

        // 채팅 대상자에게 메세지 전송
        messagingTemplate.convertAndSendToUser(message.getTo(), "/chat/subscribe", message.getMessage());
    }

//    private ChatRoom findExistingChatRoom(ChatRoomRequest request) {
//        Member fromMember = memberFinder.getMemberById(request.getFrom());
//        Set<ChatParticipant> chatParticipants = fromMember.getChatParticipants();
//
//    }

    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }
}
