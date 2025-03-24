package org.demo.server.module.review.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.dto.PagedListResponse;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        // Todo Temp 폴더에서 -> 실제 이미지를 reviews > username > 날짜로 옮겨야 한다


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
    // 기본적으로 서버에 임시 이미지 파일이 저장되면 에디터에서는 이제 자동으로 표시된다
    @PostMapping("/images/content-temp-image")
    public ResponseEntity<FileDetails> saveTempImagesForReview(MultipartRequest multipartRequest) {
        MultipartFile tempImageFile = multipartRequest.getFile("upload");

        // 파일 저장
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = LocalDate.now().format(formatter);
        FileDetails fileDetails = fileUtils.saveFile(tempImageFile, UploadDirectory.TEMPS, formattedDate);

        return ResponseEntity.ok().body(fileDetails);
    }

    // 리뷰 임시 이미지 파일 조회
    @GetMapping("/images/content-temp-image/{tempImageFileName}")
    public ResponseEntity<?> saveTempImagesForReview(@PathVariable("tempImageFileName") String fileName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = LocalDate.now().format(formatter);

        Resource resource = new FileSystemResource(
                fileUtils.getUploadDirectory(UploadDirectory.TEMPS, formattedDate) + fileName
        );
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Disposition", "attachment; filename=" + resource.getFile().toPath());
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().headers(headers).body(resource);
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
}
