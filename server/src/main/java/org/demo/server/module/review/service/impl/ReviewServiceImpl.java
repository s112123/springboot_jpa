package org.demo.server.module.review.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewImageForm;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.dto.form.ReviewUpdateForm;
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

@Slf4j
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
     * 리뷰 목록
     * sort 옵션은 전체 (0), 최신 순 (1), 평점 순 (2)
     *
     * @param page 현재 페이지
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 createdAt 으로 내림차순
     */
    @Override
    public Page<ReviewDetails> findAll(int page, int sort) {
        // 전체 → sort = 0
        String sortOption = "createdAt";
        switch (sort) {
            case 1:
                // 최근 순
                sortOption = "createdAt"; break;
            case 2:
                // 평점 순
                sortOption = "star"; break;
        }
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, sortOption));
        Page<Review> findReviews = reviewRepository.findAll(pageable);
        Page<ReviewDetails> findReviewsDetails = findReviews.map(review -> review.toDetails());
        return findReviewsDetails;
    }

    /**
     * 리뷰 목록
     * sort 옵션은 전체 (0, "createdAt"), 최신 순 (1, "createdAt"), 평점 순 (2, "star")
     *
     * @param page 현재 페이지
     * @param sort 정렬 기준
     * @param searchKeyword 검색어
     * @return 검색된 결과에서 현재 페이지에 해당하는 10개의 리뷰 목록을 sort 로 내림차순
     */
    @Override
    public Page<ReviewDetails> findAll(int page, int sort, String searchKeyword) {
        // 전체 → sort = 0
        String sortOption = "createdAt";
        switch (sort) {
            case 1:
                // 최근 순
                sortOption = "createdAt"; break;
            case 2:
                // 평점 순
                sortOption = "star"; break;
        }
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, sortOption));
        Page<Review> findReviews = reviewRepository.findAll(searchKeyword, pageable);
        Page<ReviewDetails> findReviewsDetails = findReviews.map(review -> review.toDetails());
        return findReviewsDetails;
    }

    /**
     * 특정 회원이 작성한 리뷰 목록
     *
     * @param memberId 회원 식별자
     * @param page 조회할 페이지 번호
     * @return 리뷰 목록
     */
    @Override
    public Page<ReviewDetails> findByMemberId(Long memberId, int page) {
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "reviewId"));
        Page<ReviewDetails> findReviews = reviewRepository.findByMember_MemberId(memberId, pageable)
                .map(review -> review.toDetails());
        return findReviews;
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
        findReview.updateHits();
        return findReview.toDetails();
    }

    /**
     * 리뷰 수정
     *
     * @param form 수정할 리뷰 정보
     * @return 수정된 리뷰 정보
     */
    @Override
    public ReviewDetails update(ReviewUpdateForm form) {
        // 리뷰 엔티티 조회
        Review findReview = reviewFinder.getReviewById(form.getReviewId());

        // 리뷰 정보 수정
        findReview.updateReview(form);

        // 리뷰 이미지 등록
        List<ReviewImageForm> savedImages = form.getSavedImageForms();
        for (int i = 0; i < savedImages.size(); i++) {
            ReviewImage reviewImage = ReviewImage.builder()
                    .originalFileName(savedImages.get(i).getOriginalFileName())
                    .savedFileName(savedImages.get(i).getSavedFileName())
                    .isThumbnail((i == 0) ? true : false)
                    .review(findReview)
                    .build();
            findReview.addReviewImage(reviewImage);
        }

        // 리뷰 내용에 사용되지 않는 기존의 이미지 삭제
        List<ReviewImageForm> deletedImages = form.getDeletedImageForms();
        deletedImages.forEach((deletedImage) -> deleteImageFile(findReview, deletedImage));

        // 서버에 있는 임시 파일을 보관할 폴더로 이동
        List<ReviewImage> reviewImages = findReview.getReviewImages();
        reviewImages.forEach((reviewImage) -> moveTempReviewImageFile(findReview, reviewImage));

        return findReview.toDetails();
    }

    /**
     * 리뷰 삭제
     * 리뷰 글에 있는 이미지까지 모두 삭제해야 한다
     * 리뷰 이미지 위치 → uploads > reviews > memberId > reviewId
     *
     * @param reviewId 삭제할 리뷰 식별자
     */
    @Override
    public void delete(Long reviewId) {
        // 서버에 저장된 이미지 파일을 삭제하기 위해서 리뷰 조회
        Review findReview = reviewFinder.getReviewById(reviewId);

        // DB 내역 삭제
        reviewRepository.deleteById(reviewId);

        // 리뷰 내용의 이미지를 저장한 디렉토리 삭제
        String subDir1 = String.valueOf(findReview.getMember().getMemberId());
        String subDir2 = String.valueOf(reviewId);
        fileUtils.deleteDirectory(UploadDirectory.REVIEWS, subDir1, subDir2);
    }

    /**
     * 선택된 리뷰 모두 삭제 (벌크)
     *
     * @param deletedReviewIds 삭제할 리뷰 식별자 목록
     */
    @Override
    public void deleteSelectedReviews(List<Long> deletedReviewIds) {
        // 리뷰 내용의 이미지를 저장한 디렉토리 삭제
        for (Long deletedReviewId : deletedReviewIds) {
            Review findReview = reviewFinder.getReviewById(deletedReviewId);
            String subDir1 = String.valueOf(findReview.getMember().getMemberId());
            String subDir2 = String.valueOf(deletedReviewId);
            fileUtils.deleteDirectory(UploadDirectory.REVIEWS, subDir1, subDir2);
        }

        // DB 내역 삭제
        reviewRepository.deleteReviewImagesByReviewIds(deletedReviewIds);
        reviewRepository.deleteByReviewIds(deletedReviewIds);
    }

    /**
     * 임시 파일을 보관 폴더로 이동
     * 임시 파일 위치: temps > memberId
     * 보관 파일 위치: reviews > memberId > reviewId
     *
     * @param review
     * @param savedReviewImage
     */
    private void moveTempReviewImageFile(Review review, ReviewImage savedReviewImage) {
        // 이동할 파일 이름
        String movedFileName = savedReviewImage.getSavedFileName();

        // 원본 폴더 경로: temps > memberId
        String sourceSubDirectory = String.valueOf(review.getMember().getMemberId());
        String sourceDirectory = fileUtils.getUploadDirectory(UploadDirectory.TEMPS, sourceSubDirectory);

        // 이동할 폴더: reviews > memberId > reviewId
        String targetSubDirectory = String.valueOf(review.getReviewId());
        String targetDirectory =
                fileUtils.getUploadDirectory(UploadDirectory.REVIEWS, sourceSubDirectory, targetSubDirectory);

        // 임시 폴더에서 보관 폴더로 이동
        fileUtils.moveFile(movedFileName, sourceDirectory, targetDirectory);
    }

    /**
     * 리뷰에 필요없는 기존의 이미지 파일 삭제
     * 보관 파일 위치: reviews > memberId > reviewId
     *
     * @param review
     * @param deletedImage
     */
    private void deleteImageFile(Review review, ReviewImageForm deletedImage) {
        // 하위 디렉토리
        String subDirectory1 = String.valueOf(review.getMember().getMemberId());
        String subDirectory2 = String.valueOf(review.getReviewId());

        // 파일 삭제
        fileUtils.deleteFile(deletedImage.getSavedFileName(), UploadDirectory.REVIEWS, subDirectory1, subDirectory2);
    }
}
