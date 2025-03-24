package org.demo.server.module.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.dto.details.MemberDetails;


import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(force = true)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private final Long memberId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private final String email;

    @Column(name = "password", nullable = false)
    private final String password;

    @Column(name = "username", nullable = false, unique = true)
    private final String username;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private final Role role;

    // Member (1)-(1) ProfileImage
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private final ProfileImage profileImage;

    private Member(Builder builder) {
        this.memberId = builder.memberId;
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
        this.role = builder.role;
        this.profileImage = builder.profileImage;
        super.updatedAt = builder.updatedAt;
    }

    /**
     * Member 엔티티를 MemberDetails 로 변환 (Entity → DTO)
     *
     * @return MemberDetails
     */
    public MemberDetails toDetails () {
        return MemberDetails.builder()
                .memberId(this.getMemberId())
                .email(this.getEmail())
                .password(this.getPassword())
                .username(this.getUsername())
                .role(this.getRole())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .profileImageDetails(this.profileImage.toDetails())
                .build();
    }

    /**
     * ProfileImage 정보를 포함하지 않고 회원 정보만 가진 MemberDetails DTO 를 생성한다 (Entity → DTO)
     *
     * @return MemberDetails DTO 를 반환한다
     */
    public MemberDetails toDetailsWithoutProfileImage () {
        return MemberDetails.builder()
                .memberId(this.getMemberId())
                .email(this.getEmail())
                .password(this.getPassword())
                .username(this.getUsername())
                .role(this.getRole())
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    /**
     * Member 엔티티를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // Member.Builder()
    public static class Builder {

        private Long memberId;
        private String email;
        private String password;
        private String username;
        private Role role;
        private LocalDateTime updatedAt;
        private ProfileImage profileImage;

        public Builder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder profileImage(ProfileImage profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Member build() {
            return new Member(this);
        }
    }
}
