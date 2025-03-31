package org.demo.server.module.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "chat_Participant")
@Getter
@NoArgsConstructor
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
}
