package org.demo.server.module.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.review.dto.details.ReviewDetails;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(force = true)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private final Long reviewId;

    @Column(name = "title", nullable = false)
    private final String title;

    @Column(name = "content", nullable = false)
    private final String content;

    @Column(name = "store_name", nullable = false)
    private final String storeName;

    @Column(name = "store_address", nullable = false)
    private final String storeAddress;

    @Column(name = "star", nullable = false)
    private final Integer star;

    @Column(name = "hits", columnDefinition = "BIGINT DEFAULT 0")
    private final Long hits;

    // Member (1) - (*) Review
    @ManyToOne
    @JoinColumn(name = "member_id")
    private final Member member;

    private Review(Builder builder) {
        this.reviewId = builder.reviewId;
        this.title = builder.title;
        this.content = builder.content;
        this.storeName = builder.storeName;
        this.storeAddress = builder.storeAddress;
        this.star = builder.star;
        this.hits = builder.hits;
        this.member = builder.member;
    }

    /**
     * Review 엔티티를 ReviewDetails 로 변환 (Entity → DTO)
     *
     * @return ReviewDetails
     */
    public ReviewDetails toDetails() {
        return ReviewDetails.builder()
                .reviewId(this.reviewId)
                .title(this.title)
                .content(this.content)
                .storeName(this.storeName)
                .storeAddress(this.storeAddress)
                .star(this.star)
                .hits(this.hits)
                .member(this.member)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * Review 엔티티를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // Review.Builder()
    public static class Builder {

        private Long reviewId;
        private String title;
        private String content;
        private String storeName;
        private String storeAddress;
        private Integer star;
        private Long hits;
        private Member member;

        public Builder reviewId(Long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder storeName(String storeName) {
            this.storeName = storeName;
            return this;
        }

        public Builder storeAddress(String storeAddress) {
            this.storeAddress = storeAddress;
            return this;
        }

        public Builder star(Integer star) {
            this.star = star;
            return this;
        }

        public Builder hits(Long hits) {
            this.hits = hits;
            return this;
        }

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public Review build() {
            return new Review(this);
        }
    }
}
