package org.demo.server.module.member.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.infra.common.util.file.FileDetails;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.infra.security.util.JwtUtils;
import org.demo.server.infra.sse.service.SseEmitterService;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.dto.form.MemberUpdateForm;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.entity.Role;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.member.util.send.impl.confirm.base.SendConfirmCode;
import org.demo.server.module.member.util.send.impl.password.base.SendTempPassword;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberFinder memberFinder;
    private final FileUtils fileUtils;
    private final SendConfirmCode sendConfirmCode;
    private final SendTempPassword sendTempPassword;
    private final JwtUtils jwtUtils;
    private final SseEmitterService sseEmitterService;
    private final RedisTemplate<String, String> redisTemplate;

    public MemberController(
            MemberService memberService,
            MemberFinder memberFinder,
            FileUtils fileUtils,
            SendConfirmCode sendConfirmCode,
            SendTempPassword sendTempPassword,
            JwtUtils jwtUtils,
            SseEmitterService sseEmitterService,
            @Qualifier("redisTemplate01") RedisTemplate<String, String> redisTemplate
    ) {
        this.memberService = memberService;
        this.memberFinder = memberFinder;
        this.fileUtils = fileUtils;
        this.sendConfirmCode = sendConfirmCode;
        this.sendTempPassword = sendTempPassword;
        this.jwtUtils = jwtUtils;
        this.sseEmitterService = sseEmitterService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 회원 등록
     *
     * @param form 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    @PostMapping
    public ResponseEntity<MemberResponse> saveMember(
            @Valid @RequestBody MemberSaveForm form, BindingResult bindingResult
    ) throws BindException {
        // 유효성 검사 (입력 값)
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        // MemberDetails → MemberResponse
        MemberResponse response = memberService.save(form).toResponse();

        // HTTP Status 에서 CREATED 는 응답 헤더 중 Location 에서 리소스 위치를 전달할 수 있다
        // Location: http://localhost:8080/api/v1/members/{username}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{username}")
                .buildAndExpand(response.getUsername())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 인증 메일 발송
     *
     * @param email 인증 코드를 보낼 메일 주소
     */
    @PostMapping("/codes")
    public ResponseEntity<String> sendConfirmationEmail(@RequestBody String email) {
        String code = sendConfirmCode.send(email);
        return ResponseEntity.ok().body(code);
    }

    /**
     * 인증 메일 확인
     * Redis 에 저장된 인증 코드와 이메일로 전송된 인증 코드를 전송하여 유효한 사용자인지 확인
     *
     * @param body 인증 코드 확인에 필요한 이메일과 인증 코드 값이 담겨있다
     */
    @PostMapping("/codes/check")
    public ResponseEntity<Void> checkConfirmationCode(@RequestBody Map<String, String> body) {
        sendConfirmCode.validate(body.get("email"), body.get("code"));
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
     * 임시 비밀번호 전송
     *
     * @param body
     * @return
     */
    @PostMapping("/send-password")
    public ResponseEntity<String> sendTempPassword(@RequestBody Map<String, String> body) {
        // 회원이 존재하는지 확인
        if (!memberService.existsEmail(body.get("email"))) {
            throw new NotFoundException("가입되지 않은 이메일 주소입니다");
        }
        String tempPassword = sendTempPassword.send(body.get("email"));
        return ResponseEntity.ok().body(tempPassword);
    }

    /**
     * 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @GetMapping("/{username}")
    public ResponseEntity<MemberResponse> findMemberByUsername(@PathVariable("username") String username) {
        MemberResponse response = memberService.findByUsername(username).toResponse();
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
        Page<MemberResponse> findMembers = memberService.findAll(page)
                .map(memberDetails -> memberDetails.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(findMembers));
    }

    /**
     * 회원 정보 수정
     *
     * @param username 회원의 기존 username
     * @param memberUpdateForm 변경된 회원 정보
     * @return 새로운 Access Token
     */
    @PatchMapping("/{username}")
    public ResponseEntity<Map<String, String>> updateMemberByUsername(
            @PathVariable("username") String username,
            @RequestBody MemberUpdateForm memberUpdateForm
    ) {
        // 회원 정보 변경
        MemberDetails memberDetails = memberService.update(username, memberUpdateForm);

        // Access Token 재발급
        Claims claims = Jwts.claims();
        claims.put("memberId", memberDetails.getMemberId());
        claims.put("username", memberDetails.getUsername());
        claims.put("roles", List.of(memberDetails.getRole()));
        String accessToken = jwtUtils.create(claims);

        // ResponseBody
        Map<String, String> responseBody = Map.of("accessToken", accessToken);
        return ResponseEntity.ok().body(responseBody);
    }

    /**
     * 권한 변경
     *
     * @param memberId 회원의 식별자
     * @param role 변경할 권한
     * @return
     */
    @PatchMapping(value = "/{memberId}/roles")
    public ResponseEntity<Void> updateRole(@PathVariable("memberId") Long memberId, @RequestBody Role role) {
        memberService.updateRole(memberId, role);
        return ResponseEntity.ok().build();
    }

    /**
     * 닉네임 중복 확인
     *
     * @param username 입력받은 닉네임
     * @return 닉네임이 중복이면 true, 중복이 아니면 false 반환
     */
    @PostMapping("/username/check")
    public ResponseEntity<Boolean> checkExistsUsername(@RequestBody String username) {
        boolean isExists = memberService.existsUsername(username);
        return ResponseEntity.ok().body(isExists);
    }

    /**
     * email 로 회원 정보 삭제
     *
     * @param email 삭제 할 회원의 email
     * @return 정상 삭제인 경우, 204 반환
     */
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteMemberByEmail(
            @PathVariable("email") String email,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 회원 식별자를 가져오기 위해 회원 조회
        Member findMember = memberFinder.getMemberByEmail(email);
        // SseEmitter 삭제
        sseEmitterService.removeEmitter(findMember.getMemberId());
        // Redis 에서 Refresh Token 제거
        redisTemplate.delete("refreshToken:member:" + findMember.getMemberId());

        // 쿠키 제거
        Cookie refreshTokenCookie = getRefreshTokenCooke(request);
        if (refreshTokenCookie != null) {
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);
        }

        // 회원 삭제
        memberService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로필 이미지 등록
     *
     * @param memberId 프로필 이미지를 등록할 회원의 memberId
     * @param multipartFile 등록할 이미지 파일
     * @return 등록된 이미지 파일 정보
     */
    @PostMapping("/profile-images")
    public ResponseEntity<FileDetails> uploadProfileImage(
            @RequestPart("memberId") String memberId,
            @RequestPart(value = "profile-file", required = false) MultipartFile multipartFile
    ) {
        FileDetails fileDetails = fileUtils.saveFile(multipartFile, UploadDirectory.PROFILES, memberId);
        return ResponseEntity.ok().body(fileDetails);
    }

    /**
     * 프로필 이미지 조회
     *
     * @param memberId 프로필 이미지를 조회할 회원의 memberId
     * @param fileName 조회할 이미지 파일 이름
     * @return 이미지 리소스
     */
    @GetMapping("/profile-images/{memberId}/{fileName}")
    public ResponseEntity<Resource> viewProfileImage(
            @PathVariable("memberId") String memberId,
            @PathVariable("fileName") String fileName
    ) {
        // 파일 경로
        String filePath = fileUtils.getUploadDirectory(UploadDirectory.PROFILES, memberId);

        // 파일 조회
        Resource resource = new FileSystemResource(filePath + fileName);
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
     * 프로필 이미지 파일 삭제
     *
     * @param memberId 프로필 이미지를 조회할 회원의 memberId
     * @param imageFileNames 삭제할 프로필 이미지 파일 이름 목록
     * @return 삭제에 성공하면 204 응답, 파일이 존재하지 않으면 404 반환
     */
    @DeleteMapping("/profile-images/{memberId}")
    public ResponseEntity<Void> deleteProfileImages(
            @PathVariable("memberId") String memberId,
            @RequestBody List<String> imageFileNames
    ) {
        fileUtils.deleteFiles(imageFileNames, UploadDirectory.PROFILES, memberId);
        return ResponseEntity.noContent().build();
    }

    // RefreshToken 쿠키 가져오기
    private Cookie getRefreshTokenCooke(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("todayReviewsRefreshToken")) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
