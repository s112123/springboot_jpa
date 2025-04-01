package org.demo.server.module.chat.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @Column(name = "message")
    private String message;

    // Member (1)-(*) ChatMessage
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // ChatRoom (1)-(*) ChatMessage
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;
}
