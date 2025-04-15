package org.demo.server.module.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "notice")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(name = "content")
    private String content;

    // Member (1)-(*) Notice
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member writer;
}
