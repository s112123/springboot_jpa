package org.demo.server.module.chat.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;

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

    /**
     * 메세지 저장
     *
     * @param message 메세지
     */
    public void addMessage(String message) {
        this.message = message;
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
