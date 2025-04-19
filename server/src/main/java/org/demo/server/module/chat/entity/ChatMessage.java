package org.demo.server.module.chat.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_message")
@Getter
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @Column(name = "message")
    private String message;

    // Member (1)-(*) ChatMessage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // ChatRoom (1)-(*) ChatMessage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    // ChatMessage (1)-(*) ChatMessageRead
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageRead> chatMessageReads = new ArrayList<>();

    /**
     * 메세지 저장
     *
     * @param senderId 메세지를 보내는 회원 ID
     * @param receiverId 메세지를 받는 회원 ID
     * @param message 메세지 내용
     */
    public void setMessage(Long senderId, Long receiverId, String message) {
        this.message = message;

        // 메세지 읽음 여부
        ChatMessageRead chatMessageRead = ChatMessageRead.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .read(false)
                .chatMessage(this)
                .build();
        this.chatMessageReads.add(chatMessageRead);
    }

    /**
     * 회원 외래키 (member_id) 설정
     *
     * @param member 메세지를 전송한 회원 식별자
     */
    public void addMember(Member member) {
        this.member = member;
        member.getChatMessages().add(this);
    }

    /**
     * 채팅방 외래키 (chat_room_id) 설정
     *
     * @param chatRoom 메세지를 전송한 채팅방 식별자
     */
    public void addChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.getChatMessages().add(this);
    }
}
