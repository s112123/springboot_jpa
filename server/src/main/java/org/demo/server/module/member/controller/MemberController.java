package org.demo.server.module.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.dto.PagedListResponse;
import org.demo.server.infra.common.util.file.FileDetails;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.member.dto.request.MemberSaveRequest;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.member.util.confirm.base.ConfirmCodeUtils;
import org.demo.server.module.member.util.confirm.dto.ConfirmCodeRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:8080/")
public class MemberController {

    private final MemberService memberService;
    private final FileUtils fileUtils;
    private final ConfirmCodeUtils confirmCodeUtils;

    /**
     * 회원 등록
     *
     * @param form 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    @PostMapping
    public ResponseEntity<MemberResponse> saveMember(
            @Valid @RequestBody MemberSaveRequest form, BindingResult bindingResult
    ) throws BindException {
        // 유효성 검사 (입력 값)
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        MemberResponse response = memberService.save(form);

        // HTTP Status 에서 CREATED 는 응답 헤더 중 Location 에서 리소스 위치를 전달할 수 있다
        // Location: http://localhost:8080/api/v1/members/1
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getMemberId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 인증 메일 발송
     *
     * @param email 인증 코드를 보낼 메일 주소
     */
    @PostMapping("/codes")
    public ResponseEntity<Map<String, String>> sendConfirmationEmail(@RequestBody String email) {
        confirmCodeUtils.sendConfirmCode(email);
        return ResponseEntity.ok().body(Map.of("status", ""));
    }

    /**
     * 인증 메일 확인
     * Redis 에 저장된 인증 코드와 이메일로 전송된 인증 코드를 전송하여 유효한 사용자인지 확인
     *
     * @param body 인증 코드 확인에 필요한 이메일과 인증 코드 값이 담겨있다
     */
    @PostMapping("/codes/check")
    public ResponseEntity<Void> checkConfirmationCode(@RequestBody Map<String, String> body) {
        confirmCodeUtils.validateConfirmCode(body.get("email"), body.get("code"));
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 중복 확인
     *
     * @param email 입력받은 이메일
     * @return 이메일이 중복이면 true, 중복이 아니면 false 반환
     */
    @PostMapping("/emails/check")
    public ResponseEntity<Boolean> checkExistsEmail(@RequestBody String email) {
        boolean isExists = memberService.existsEmail(email);
        return ResponseEntity.ok().body(isExists);
    }

    /**
     * memberId 로 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> findMemberById(@PathVariable("memberId") Long memberId) {
        MemberResponse response = memberService.findById(memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 회원 목록 조회
     *
     * @return createdAt 을 기준으로 내림차순으로 정렬한 회원 목록
     */
    @GetMapping("/pages/{page}")
    public ResponseEntity<PagedListResponse<MemberResponse>> findAll(@PathVariable("page") int page) {
        // 회원 목록
        PagedListResponse<MemberResponse> findMembers = memberService.findAll(page);
        return ResponseEntity.ok().body(findMembers);
    }

    /**
     * memberId 로 회원 정보 삭제
     *
     * @param memberId 삭제 할 회원의 memberId
     * @return 정상 삭제인 경우, 반환값이 없고 삭제하려는 회원이 존재하지 않으면 NotFoundException
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMemberById(@PathVariable("memberId") Long memberId) {
        memberService.deleteById(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 프로필 이미지 등록
     *
     * @param multipartFile 등록할 이미지 파일
     * @return 등록된 이미지 파일 정보
     */
    @PostMapping("/profile-images")
    public ResponseEntity<FileDetails> uploadProfileImage(
            @RequestPart(value = "file", required = false) MultipartFile multipartFile
    ) {
        FileDetails fileDetails = fileUtils.saveFile(multipartFile, UploadDirectory.PROFILES);
        return ResponseEntity.ok().body(fileDetails);
    }

    /**
     * 프로필 이미지 조회
     *
     * @param fileName 조회할 이미지 파일 이름
     * @return 이미지 리소스
     */
    @GetMapping("/profile-images/{fileName}")
    public ResponseEntity<Resource> viewProfileImage(@PathVariable("fileName") String fileName) {
        Resource resource = new FileSystemResource(fileUtils.getUploadDirectory(UploadDirectory.PROFILES) + fileName);
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.add("Content-Disposition", "attachment; filename=" + resource.getFile().toPath());
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
