package org.demo.server.module.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // ChatRoom (1)-(*) ChatParticipant
    @OneToMany(mappedBy = "chatRoom")
    private Set<ChatParticipant> chatParticipants = new HashSet<>();

    // ChatRoom (1)-(*) ChatMessage
    @OneToMany(mappedBy = "chatRoom")
    private Set<ChatMessage> chatMessages = new HashSet<>();
}
