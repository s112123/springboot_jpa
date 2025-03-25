package org.demo.server.module.review.service.impl;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewImageForm;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.entity.Review;
import org.demo.server.module.review.entity.ReviewImage;
import org.demo.server.module.review.repository.ReviewRepository;
import org.demo.server.module.review.service.base.ReviewFinder;
import org.demo.server.module.review.service.base.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileUtils fileUtils;
    private final MemberFinder memberFinder;
    private final ReviewFinder reviewFinder;

    /**
     * 리뷰 등록
     *
     * @param form 입력된 리뷰 정보
     * @return 저장된 리뷰 정보
     */
    @Override
    public ReviewDetails save(ReviewSaveForm form) {
        // 회원 엔티티 조회
        Member findMember = memberFinder.getMemberByUsername(form.getWriter());

        // 리뷰 엔티티 생성
        Review review = Review.builder()
                .title(form.getTitle())
                .content(form.getContent())
                .storeName(form.getStoreName())
                .storeAddress(form.getStoreAddress())
                .star(form.getStar())
                .hits(0L)
                .member(findMember)
                .build();

        // 리뷰 이미지 엔티티
        List<ReviewImageForm> reviewImageForms = form.getReviewImageForms();
        for (int i = 0; i < reviewImageForms.size(); i++) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .originalFileName(reviewImageForms.get(i).getOriginalFileName())
                    .savedFileName(reviewImageForms.get(i).getSavedFileName())
                    .isThumbnail((i == 0) ? true : false)
                    .review(review)
                    .build();
            review.addReviewImage(reviewImage);
        }

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 서버에 있는 임시 파일을 보관할 폴더로 이동
        List<ReviewImage> savedReviewImages = savedReview.getReviewImages();
        for (ReviewImage savedReviewImage : savedReviewImages) {
            moveTempReviewImageFile(savedReview, savedReviewImage);
        }

        return savedReview.toDetails();
    }

    /**
     * 리뷰 목록
     *
     * @param page 현재 페이지
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 createdAt 으로 내림차순
     */
    @Override
    public Page<ReviewDetails> findAll(int page) {
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> findReviews = reviewRepository.findAll(pageable);
        Page<ReviewDetails> findReviewsDetails = findReviews.map(review -> review.toDetails());
        return findReviewsDetails;
    }

    /**
     * 리뷰 조회
     *
     * @param reviewId 조회 할 reviewId
     * @return 조회된 리뷰
     */
    @Override
    public ReviewDetails findById(long reviewId) {
        Review findReview = reviewFinder.getReviewById(reviewId);
        return findReview.toDetails();
    }

    /**
     * 임시 파일을 보관 폴더로 이동
     * 임시 파일 위치: temps > memberId
     * 보관 파일 위치: reviews > memberId > reviewId
     *
     * @param savedReview
     * @param savedReviewImage
     */
    private void moveTempReviewImageFile(Review savedReview, ReviewImage savedReviewImage) {
        // 이동할 파일 이름
        String movedFileName = savedReviewImage.getSavedFileName();

        // 원본 폴더 경로: temps > memberId
        String sourceSubDirectory = String.valueOf(savedReview.getMember().getMemberId());
        String sourceDirectory = fileUtils.getUploadDirectory(UploadDirectory.TEMPS, sourceSubDirectory);

        // 이동할 폴더: reviews > memberId > reviewId
        String targetSubDirectory = String.valueOf(savedReview.getReviewId());
        String targetDirectory =
                fileUtils.getUploadDirectory(UploadDirectory.REVIEWS, sourceSubDirectory, targetSubDirectory);

        // 임시 폴더에서 보관 폴더로 이동
        fileUtils.moveFile(movedFileName, sourceDirectory, targetDirectory);
    }
}
