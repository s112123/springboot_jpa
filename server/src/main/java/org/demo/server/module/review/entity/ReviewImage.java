package org.demo.server.module.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_image")
@Getter
@NoArgsConstructor(force = true)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private final Long reviewImageId;

    @Column(name = "original_file_name")
    private final String originalFileName;

    @Column(name = "saved_file_name")
    private final String savedFileName;

    @Column(name = "path")
    private final String path;

    @Column(name = "is_thumbnail")
    private final Boolean isThumbnail;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private final Review review;

    private ReviewImage(Builder builder) {
        this.reviewImageId = builder.reviewImageId;
        this.originalFileName = builder.originalFileName;
        this.savedFileName = builder.savedFileName;
        this.path = builder.path;
        this.isThumbnail = builder.isThumbnail;
        this.review = builder.review;
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
        private String path;
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

        public Builder path(String path) {
            this.path = path;
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

