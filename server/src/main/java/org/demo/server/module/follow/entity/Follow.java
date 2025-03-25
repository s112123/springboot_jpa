package org.demo.server.module.follow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;

@Entity
@Table(name = "follow")
@Getter
@NoArgsConstructor
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private Member follower;

    @ManyToOne
    @JoinColumn(name = "followed_id")
    private Member followed;

    private Follow(Builder builder) {
        this.followId = builder.followId;
        this.followed = builder.followed;
        this.follower = builder.follower;
    }

    /**
     * Follow 엔티티를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // Follow.builder()
    public static class Builder {

        private Long followId;
        private Member follower;
        private Member followed;

        public Builder followId(Long followId) {
            this.followId = followId;
            return this;
        }

        public Builder follower(Member follower) {
            this.follower = follower;
            return this;
        }

        public Builder followed(Member followed) {
            this.followed = followed;
            return this;
        }

        public Follow build() {
            return new Follow(this);
        }
    }
}
