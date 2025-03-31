package org.demo.server.module.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.chat.entity.ChatMessage;
import org.demo.server.module.chat.entity.ChatParticipant;
import org.demo.server.module.follow.entity.Follow;
import org.demo.server.module.good.entity.Good;
import org.demo.server.module.member.dto.details.MemberDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    // Member (1)-(1) ProfileImage
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileImage profileImage;

    // Member (1)-(*) Good (*)-(1) Review
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Good> goods = new ArrayList<>();

    // Member (1)-(*) Follow (*)-(1) Member
    // 팔로워 (Follower) → A 가 B 를 친구 추가하면 A 는 B 의 팔로워이다
    // 팔로우 (Follow) → A 가 B 를 친구 추가하면 A 가 B 를 팔로우 했다고 한다
    // memberId 가 1인 사람이 2, 3을 친구 추가했다고 가정하자
    // 그러면 Follow 테이블에 follower_id 에는 1, followed_id 에는 2, 3 이 된다
    // 1이 친구 추가한 (팔로우) 사람을 찾으려면 follower_id 가 1인 사람의 followed_id 를 찾으면 된다
    // 1을 친구 추가한 (팔로워) 사람을 찾으려면 followed_id 가 1인 사람의 follower_id 를 찾으면 된다
    // 이 필드는 나를 팔로우 한, 나의 팔로워들의 찾는 것이므로 내가 팔로우 당한 것이다
    // 즉, 나의 member_id 가 Follow 테이블에서 외래키인 followd_id 에 저장되어야 한다
    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers = new HashSet<>();

    // Member (1)-(*) Follow (*)-(1) Member
    // 이 필드는 내가 팔로우 한 사람들을 찾는 것이므로 내가 팔로워가 되는 것이다
    // 즉, 나의 member_id 가 Follow 테이블에서 외래키인 follower_id 에 저장되어야 한다
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    // Member (1)-(*) ChatParticipant
    @OneToMany(mappedBy = "member")
    private Set<ChatParticipant> chatParticipants = new HashSet<>();

    // Member (1)-(*) ChatMessage
    @OneToMany(mappedBy = "member")
    private Set<ChatMessage> chatMessages = new HashSet<>();

    private Member(Builder builder) {
        this.memberId = builder.memberId;
        this.email = builder.email;
        this.password = builder.password;
        this.username = builder.username;
        this.role = builder.role;
        this.profileImage = builder.profileImage;
        this.goods = builder.goods;
        this.followers = builder.followers;
        this.following = builder.following;
    }

    /**
     * 프로필 이미지 추가
     *
     * @param profileImage 회원에 해당하는 프로필 이미지
     */
    public void addProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * 닉네임 변경
     *
     * @param username 변경할 닉네임
     */
    public void updateUsername(String username) {
        this.username = username;
    }

    /**
     * 비밀번호 변경
     * 
     * @param password 변경할 비밀번호
     */
    public void updatePassword(String password) {
        this.password = password;
    }

    /**
     * 권한 변경
     *
     * @param role 변경할 권한
     */
    public void updateRole(Role role) {
        this.role = role;
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
        private ProfileImage profileImage;
        private List<Good> goods = new ArrayList<>();
        private Set<Follow> followers = new HashSet<>();
        private Set<Follow> following = new HashSet<>();

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

        public Builder profileImage(ProfileImage profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder goods(List<Good> goods) {
            this.goods = goods;
            return this;
        }

        public Builder followers(Set<Follow> followers) {
            this.followers = followers;
            return this;
        }

        public Builder following(Set<Follow> following) {
            this.following = following;
            return this;
        }

        public Member build() {
            return new Member(this);
        }
    }
}
