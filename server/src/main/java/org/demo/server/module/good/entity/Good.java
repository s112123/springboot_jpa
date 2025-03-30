package org.demo.server.module.good.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.demo.server.module.good.dto.response.GoodResponse;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.review.entity.Review;

@Entity
@Table(name = "good")
@Getter
@NoArgsConstructor
public class Good {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "good_id")
    private Long goodId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    private Good(Builder builder) {
        this.goodId = builder.goodId;
        this.member = builder.member;
        this.review = builder.review;
    }

    /**
     * Good 엔티티를 GoodResponse 로 변환 (Entity → DTO)
     *
     * @return GoodResponse
     */
    public GoodResponse toResponse() {
        return new GoodResponse(this);
    }

    /**
     * Good 엔티티를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // Good.Builder()
    public static class Builder {

        private Long goodId;
        private Member member;
        private Review review;

        public Builder goodId(Long goodId) {
            this.goodId = goodId;
            return this;
        }

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public Builder review(Review review) {
            this.review = review;
            return this;
        }

        public Good build() {
            return new Good(this);
        }
    }
}
