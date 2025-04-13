package org.demo.server.infra.mq.entity;

import jakarta.persistence.*;
import lombok.*;
import org.demo.server.module.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member consumer;

    /**
     * 읽음 처리
     *
     * @param read 읽음 여부
     */
    public void setRead(boolean read) {
        this.read = read;
    }
}
