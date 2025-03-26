package org.demo.server.module.review.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.infra.common.util.file.FileDetails;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.review.dto.details.ReviewDetails;
import org.demo.server.module.review.dto.form.ReviewSaveForm;
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
        log.info("form={}", form);
        // Todo @Valid + BindingResult
        // 리뷰 등록
        ReviewResponse response = reviewService.save(form).toResponse();

        // HTTP Status 에서 CREATED 는 응답 헤더 중 Location 에서 리소스 위치를 전달할 수 있다
        // Location: http://localhost:8080/api/v1/reviews/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand("1")
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // 리뷰를 작성 중에 사용되는 임시 이미지 파일을 서버에 저장
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

    // 리뷰의 임시 이미지 파일 조회
    // 기본적으로 서버에 임시 이미지 파일이 저장되면 에디터에서는 Base64 로 인코딩해서 이미지로 보여준다
    // 하지만 현재는 서버에서 직접 조회하여 이미지를 표시하고 불필요한 임시 이미지는 최종 등록시에 삭제해야 한다
    @GetMapping("/content-images/temp/{memberId}/{tempImageFileName}")
    public ResponseEntity<?> viewTempImagesForReview(
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

    // 리뷰의 이미지 파일 조회
    @GetMapping("/content-images/{memberId}/{tempImageFileName}")
    public ResponseEntity<?> saveTempImagesForReview(
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
     *
     * @param page 현재 페이지
     * @return 현재 페이지에 해당하는 10개의 리뷰 목록을 createdAt 으로 내림차순
     */
    @GetMapping("/pages/{page}")
    public ResponseEntity<PagedListResponse<ReviewResponse>> findAllReviews(@PathVariable("page") int page) {
        Page<ReviewResponse> findReviews = reviewService.findAll(page)
                .map(reviewDetails -> reviewDetails.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(findReviews));
    }

    // 리뷰 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> findReviewById(@PathVariable("reviewId") long reviewId) {
        ReviewDetails reviewDetails = reviewService.findById(reviewId);
        return ResponseEntity.ok().body(reviewDetails.toResponse());
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
