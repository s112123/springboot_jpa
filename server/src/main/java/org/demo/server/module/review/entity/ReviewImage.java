package org.demo.server.module.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.review.dto.details.ReviewImageDetails;

@Entity
@Table(name = "review_image")
@Getter
@NoArgsConstructor(force = true)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long reviewImageId;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "saved_file_name")
    private String savedFileName;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    // Review (1) (*) ReviewImage
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    private ReviewImage(Builder builder) {
        this.reviewImageId = builder.reviewImageId;
        this.originalFileName = builder.originalFileName;
        this.savedFileName = builder.savedFileName;
        this.isThumbnail = builder.isThumbnail;
        this.review = builder.review;
    }

    /**
     * 리뷰 저장
     *
     * @param review 저장할 리뷰 정보
     */
    public void addReview(Review review) {
        this.review = review;
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview() {
        this.review = null;
    }

    /**
     * ReviewImage 엔티티를 ReviewImageDetails 로 변환 (Entity → DTO)
     *
     * @return ReviewDetails
     */
    public ReviewImageDetails toDetails() {
        return ReviewImageDetails.builder()
                .reviewImageId(this.reviewImageId)
                .originalFileName(this.originalFileName)
                .savedFileName(this.savedFileName)
                .isThumbnail(this.isThumbnail)
                .build();
    }

    /**
     * ReviewImage 엔티티를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // ReviewImage.builder()
    public static class Builder {

        private Long reviewImageId;
        private String originalFileName;
        private String savedFileName;
        private Boolean isThumbnail;
        private Review review;

        public Builder reviewImageId(Long reviewImageId) {
            this.reviewImageId = reviewImageId;
            return this;
        }

        public Builder originalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
            return this;
        }

        public Builder savedFileName(String savedFileName) {
            this.savedFileName = savedFileName;
            return this;
        }

        public Builder isThumbnail(boolean isThumbnail) {
            this.isThumbnail = isThumbnail;
            return this;
        }

        public Builder review(Review review) {
            this.review = review;
            return this;
        }

        public ReviewImage build() {
            return new ReviewImage(this);
        }
    }
}

