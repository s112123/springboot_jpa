package org.demo.server.module.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_room")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    // ChatRoom (1)-(*) ChatParticipant
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatParticipant> chatParticipants = new HashSet<>();

    // ChatRoom (1)-(*) ChatMessage
//    @OneToMany(mappedBy = "chatRoom")
//    private Set<ChatMessage> chatMessages = new HashSet<>();

    // 채팅 참여자 등록
    public void addChatParticipant(ChatParticipant chatParticipant) {
        this.chatParticipants.add(chatParticipant);
        chatParticipant.addChatRoom(this);
    }
}
