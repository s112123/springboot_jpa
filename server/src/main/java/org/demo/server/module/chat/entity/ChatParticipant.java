package org.demo.server.module.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "chat_Participant")
@Getter
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_participant_id")
    private Long chatParticipantId;

    // ChatRoom (1)-(*) ChatParticipant
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    // Member (1)-(*) ChatParticipant
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * chat_room_id 외래키 지정
     *
     * @param chatRoom 채팅방
     */
    public void addChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.getChatParticipants().add(this);
    }

    /**
     * member_id 외래키 지정
     *
     * @param member 채팅 참여자
     */
    public void addMember(Member member) {
        this.member = member;
        member.getChatParticipants().add(this);
    }
}
