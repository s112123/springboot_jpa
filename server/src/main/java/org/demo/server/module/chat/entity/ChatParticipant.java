package org.demo.server.module.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "chat_Participant")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "chatRoom")
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

    // 채팅방 등록
    public void addChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
