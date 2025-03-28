package org.demo.server.module.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.demo.server.infra.common.entity.BaseEntity;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.details.ReviewImageDetails;
import org.demo.server.module.review.dto.form.ReviewUpdateForm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor
@ToString
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_address", nullable = false)
    private String storeAddress;

    @Column(name = "star", nullable = false)
    private Integer star;

    @Column(name = "hits", columnDefinition = "BIGINT DEFAULT 0")
    private Long hits;

    // Member (1)-(*) Review
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // Review (1)-(*) ReviewImage
    // final 키워드를 사용하면 @NoArgsConstructor(force = true) 에서 null 로 초기화된다
    // 그래서 이 엔티티에서는 final 키워드를 제외했다
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();

    private Review(Builder builder) {
        this.reviewId = builder.reviewId;
        this.title = builder.title;
        this.content = builder.content;
        this.storeName = builder.storeName;
        this.storeAddress = builder.storeAddress;
        this.star = builder.star;
        this.hits = builder.hits;
        this.member = builder.member;
        this.reviewImages = builder.reviewImages;
    }

    /**
     * 조회 수 증가
     */
    public void updateHits() {
        this.hits = this.hits + 1;
    }

    /**
     * 리뷰 정보 수정
     *
     * @param form 수정할 리뷰 정보
     */
    public void updateReview(ReviewUpdateForm form) {
        this.title = form.getTitle();
        this.content = form.getContent();
        this.storeName = form.getStoreName();
        this.storeAddress = form.getStoreAddress();
        this.star = form.getStar();
        deleteReviewImages();
    }

    /**
     * Review 엔티티에 ReviewImage 엔티티를 추가하는 연관 메서드
     *
     * @param reviewImage ReviewImage
     */
    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
        reviewImage.addReview(this);
    }

    /**
     * ReviewImages 제거
     */
    private void deleteReviewImages() {
        this.reviewImages.forEach(reviewImage -> reviewImage.deleteReview());
        this.reviewImages.clear();
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
                .memberDetails(this.member.toDetails())
                .reviewImagesDetailsList(this.reviewImages.stream()
                        .map(reviewImage -> reviewImage.toDetails())
                        .collect(Collectors.toList()))
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
        private List<ReviewImage> reviewImages = new ArrayList<>();

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

        public Builder reviewImages(List<ReviewImage> reviewImages) {
            this.reviewImages = reviewImages;
            return this;
        }

        public Review build() {
            return new Review(this);
        }
    }
}
