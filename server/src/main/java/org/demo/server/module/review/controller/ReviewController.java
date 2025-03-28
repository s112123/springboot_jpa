package org.demo.server.module.review.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.infra.common.util.file.FileDetails;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
import org.demo.server.module.review.dto.form.ReviewUpdateForm;
import org.demo.server.module.review.dto.response.ReviewResponse;
import org.demo.server.module.review.service.base.ReviewService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final FileUtils fileUtils;

    /**
     * 리뷰 등록
     *
     * @param form 입력된 리뷰 정보
     * @return 저장된 리뷰 정보
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> saveReview(@RequestBody ReviewSaveForm form) {
        // Todo @Valid + BindingResult
        // 리뷰 등록
        ReviewResponse response = reviewService.save(form).toResponse();

        // HTTP Status 에서 CREATED 는 응답 헤더 중 Location 에서 리소스 위치를 전달할 수 있다
        // Location: http://localhost:8080/api/v1/reviews/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{reviewId}")
                .buildAndExpand(response.getReviewId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 리뷰를 작성 중에 사용되는 임시 이미지 파일을 서버에 저장
     *
     * @param memberId 리뷰를 작성한 회원 식별자
     * @param multipartRequest 이미지 파일
     * @return 이미지 파일 정보
     */
    @PostMapping("/content-images/temp/{memberId}")
    public ResponseEntity<FileDetails> saveTempImagesForReview(
            @PathVariable("memberId") String memberId,
            MultipartRequest multipartRequest
    ) {
        // upload 이름으로 파일이 넘어온다
        MultipartFile tempImageFile = multipartRequest.getFile("upload");
        // 파일 저장
        // 임시 파일 저장 경로: temps > memberId
        FileDetails fileDetails = fileUtils.saveFile(tempImageFile, UploadDirectory.TEMPS, memberId);
        return ResponseEntity.ok().body(fileDetails);
    }

    /**
     * 리뷰의 임시 이미지 파일 조회
     * 기본적으로 서버에 임시 이미지 파일이 저장되면 에디터에서는 Base64 로 인코딩해서 이미지로 보여준다
     * 하지만 현재는 서버에서 직접 조회하여 이미지를 표시하고 불필요한 임시 이미지는 최종 등록시에 삭제해야 한다
     *
     * @param memberId 리뷰를 작성한 회원 식별자
     * @param fileName 이미지 파일 이름
     * @return 이미지 리소스
     */
    @GetMapping("/content-images/temp/{memberId}/{tempImageFileName}")
    public ResponseEntity<Resource> viewTempImagesForReview(
            @PathVariable("memberId") String memberId,
            @PathVariable("tempImageFileName") String fileName
    ) {
        // 파일 조회
        // 임시 파일 조회 경로: temps > memberId
        Resource resource = new FileSystemResource(
                fileUtils.getUploadDirectory(UploadDirectory.TEMPS, memberId) + fileName
        );
        return ResponseEntity.ok().headers(getHttpHeaderForImage(resource)).body(resource);
    }

    /**
     * 리뷰의 이미지 파일 조회
     *
     * @param memberId 리뷰를 작성한 회원 식별자
     * @param fileName 이미지 파일 이름
     * @param reviewId 리뷰 식별자
     * @return 이미지 리소스
     */
    @GetMapping("/content-images/{memberId}/{tempImageFileName}")
    public ResponseEntity<Resource> viewImagesForReview(
            @PathVariable("memberId") String memberId,
            @PathVariable("tempImageFileName") String fileName,
            @RequestParam("reviewId") String reviewId
    ) {
        // 파일 조회
        // 파일 저장 경로: reviews > memberId > reviewId
        Resource resource = new FileSystemResource(
                fileUtils.getUploadDirectory(UploadDirectory.REVIEWS, memberId, reviewId) + fileName
        );
        return ResponseEntity.ok().headers(getHttpHeaderForImage(resource)).body(resource);
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
    @GetMapping("/pages/{page}")
    public ResponseEntity<PagedListResponse<ReviewResponse>> findAllReviews(
            @PathVariable("page") int page,
            @RequestParam(name = "sort", required = false, defaultValue = "0") int sort,
            @RequestParam(name = "searchKeyword", required = false, defaultValue = "") String searchKeyword
    ) {
        log.info("sort={}", sort);
        log.info("searchKeyword={}", searchKeyword);
        Page<ReviewResponse> findReviews = reviewService.findAll(page, sort, searchKeyword)
                .map(reviewDetails -> reviewDetails.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(findReviews));
    }

    /**
     * 특정 회원의 리뷰 목록
     *
     * @param memberId 회원 식별자
     * @param page 조회할 페이지
     * @return 리뷰 목록 정보
     */
    @GetMapping("/my/{memberId}/pages/{page}")
    public ResponseEntity<PagedListResponse<ReviewResponse>> findReviewsByMemberId(
            @PathVariable("memberId") Long memberId,
            @PathVariable("page") int page
    ) {
        Page<ReviewResponse> findReviews = reviewService.findByMemberId(memberId, page)
                .map(reviewDetails -> reviewDetails.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(findReviews));
    }

    /**
     * 리뷰 조회
     *
     * @param reviewId 리뷰 식별자
     * @return 조회된 리뷰 정보
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> findReviewById(@PathVariable("reviewId") long reviewId) {
        ReviewDetails reviewDetails = reviewService.findById(reviewId);
        return ResponseEntity.ok().body(reviewDetails.toResponse());
    }

    /**
     * 리뷰 수정
     *
     * @param form 수정할 리뷰 정보
     */
    @PatchMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(@RequestBody ReviewUpdateForm form) {
        // Todo @Valid + BindingResult
        // 리뷰 수정
        reviewService.update(form);
        return ResponseEntity.ok().build();
    }

    /**
     * 하나의 리뷰 삭제
     * 리뷰 글에 있는 이미지까지 모두 삭제해야 한다
     * 리뷰 이미지 위치 → uploads > reviews > memberId > reviewId
     *
     * @param reviewId 삭제할 리뷰 식별자
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 여러 리뷰 삭제
     * 리뷰 글에 있는 이미지까지 모두 삭제해야 한다
     * 리뷰 이미지 위치 → uploads > reviews > memberId > reviewId
     *
     * @param deletedReviewIds 삭제할 리뷰 식별자
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteSelectedReviews(@RequestBody List<Long> deletedReviewIds) {
        reviewService.deleteSelectedReviews(deletedReviewIds);
        return ResponseEntity.noContent().build();
    }

    /**
     * 이미지 전송을 위한 HTTP 헤더
     *
     * @param resource 전송할 이미지 파일
     * @return HTTP 헤더
     */
    private HttpHeaders getHttpHeaderForImage(Resource resource) {
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Disposition", "attachment; filename=" + resource.getFile().toPath());
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return headers;
    }
}
