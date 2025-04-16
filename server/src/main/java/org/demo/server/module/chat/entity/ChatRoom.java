package org.demo.server.module.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_room")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "chat_room_name")
    private String chatRoomName;

    // ChatRoom (1)-(*) ChatParticipant
    @OneToMany(mappedBy = "chatRoom")
    @Builder.Default
    private Set<ChatParticipant> chatParticipants = new HashSet<>();

    // ChatRoom (1)-(*) ChatMessage
    @OneToMany(mappedBy = "chatRoom")
    @Builder.Default
    private Set<ChatMessage> chatMessages = new HashSet<>();
}
