package org.demo.server.module.member.service.impl;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.dto.form.MemberUpdateForm;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.entity.ProfileImage;
import org.demo.server.module.member.entity.Role;
import org.demo.server.module.member.exception.DuplicatedEmailException;
import org.demo.server.module.member.repository.MemberRepository;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.member.service.base.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberFinder memberFinder;
    private final FileUtils fileUtils;

    /**
     * 회원 등록
     *
     * @param form 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    @Override
    public MemberDetails save(MemberSaveForm form) {
        // 이메일 중복 체크
        if (existsEmail(form.getEmail())) {
            throw new DuplicatedEmailException("이미 존재하는 이메일입니다");
        }

        // 기본 프로필 이미지 등록
        ProfileImage profileImage = ProfileImage.builder()
                .originalFileName("default.png")
                .savedFileName("default.png")
                .path("/src/main/resources/static/images/profiles")
                .build();

        // 회원 등록
        Member member = Member.builder()
                .email(form.getEmail())
                .password(passwordEncoder.encode(form.getPassword()))
                .username("user-" + UUID.randomUUID())
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .profileImage(profileImage)
                .build();
        Member savedMember = memberRepository.save(member);
        return savedMember.toDetails();
    }

    /**
     * memberId 로 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @Override
    @Transactional(readOnly = true)
    public MemberDetails findById(Long memberId) {
        Member findMember = memberFinder.getMemberById(memberId);
        return findMember.toDetails();
    }

    /**
     * username 로 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @Override
    @Transactional(readOnly = true)
    public MemberDetails findByUsername(String username) {
        Member findMember = memberFinder.getMemberByUsername(username);
        return findMember.toDetails();
    }

    /**
     * 회원 목록 조회
     *
     * @return createdAt 을 기준으로 내림차순으로 정렬한 회원 목록과 회원 수
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MemberDetails> findAll(int page) {
        // 요청 페이지 번호가 1보다 작으면 에러가 발생한다
        if (page < 1) {
            throw new IllegalArgumentException("존재하지 않는 페이지입니다");
        }

        // 회원 목록 조회
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MemberDetails> findMembers = memberRepository.findAll(pageable)
                .map(member -> member.toDetailsWithoutProfileImage());
        return findMembers;
    }

    /**
     * 회원 정보 수정
     *
     * @param username 회원 정보를 수정할 회원의 닉네임
     * @param form 수정할 회원 정보 내용
     * @return 수정 완료된 내용
     */
    @Override
    public MemberDetails update(String username, MemberUpdateForm form) {
        // 기존 엔티티를 영속성 컨텍스트에 넣는다
        Member findMember = memberFinder.getMemberByUsername(username);

        // 업데이트 정보
        String newUsername = form.getUsername();
        String newPassword = (form.getPassword() != null) ?
                passwordEncoder.encode(form.getPassword()) : findMember.getPassword();
        String newOriginalFileName = (form.getOriginalFileName() != null) ?
                form.getOriginalFileName() : findMember.getProfileImage().getOriginalFileName();
        String newSavedFileName = (form.getSavedFileName() != null) ?
                form.getSavedFileName() : findMember.getProfileImage().getSavedFileName();

        // ProfileImage 의 변경 정보
        ProfileImage profileImage = ProfileImage.builder()
                .profileImageId(findMember.getProfileImage().getProfileImageId())
                .originalFileName(newOriginalFileName)
                .savedFileName(newSavedFileName)
                .path(fileUtils.getUploadDirectory(UploadDirectory.PROFILES))
                .build();

        // 기존 엔티티와 memberId 가 동일한 새로운 엔티티 객체를 만든다
        Member member = Member.builder()
                .memberId(findMember.getMemberId())
                .email(findMember.getEmail())
                .password(newPassword)
                .username(newUsername)
                .role(findMember.getRole())
                .profileImage(profileImage)
                .build();

        // @Transactional 안에 있지만 현재 엔티티는 final 로 불변이므로 변경 감지를 활용할 수 없다
        // 변경 감지는 final 이 없고 Setter 와 같이 엔티티의 속성 값을 변경할 수 있어야 한다
        Member updatedMember = memberRepository.save(member);
        return updatedMember.toDetails();
    }

    /**
     * 회원의 비밀번호를 임시 비밀번호로 변경
     *
     * @param email 회원 이메일
     * @param tempPassword 임시 비밀번호
     */
    @Override
    public void updateTempPasswordByEmail(String email, String tempPassword) {
        Member findMember = memberFinder.getMemberByEmail(email);
        Member updatedMember = Member.builder()
                .memberId(findMember.getMemberId())
                .email(findMember.getEmail())
                .username(findMember.getUsername())
                .password(passwordEncoder.encode(tempPassword))
                .role(findMember.getRole())
                .profileImage(findMember.getProfileImage())
                .build();
        memberRepository.save(updatedMember);
    }

    /**
     * email 로 회원 정보 삭제
     *
     * @param email 삭제 할 회원의 email
     */
    @Override
    public void deleteByEmail(String email) {
        // 회원 조회
        Member findMember = memberFinder.getMemberByEmail(email);

        // 프로필 이미지 파일 삭제
        String profileImageName = findMember.getProfileImage().getSavedFileName();
        if (!profileImageName.equals("default.png")) {
            fileUtils.deleteFile(profileImageName, UploadDirectory.PROFILES);
        }

        // todo: 게시물 첨부 파일 삭제 처리

        // 회원 삭제
        memberRepository.deleteByEmail(email);
    }

    /**
     * 이메일이 존재하는지 여부
     *
     * @param email 존재하는지 확인 할 이메일
     * @return 존재하면 true, 존재하지 않으면 false
     */
    @Override
    public boolean existsEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임이 존재하는지 여부
     *
     * @param username 존재하는지 확인 할 닉네임
     * @return 존재하면 true, 존재하지 않으면 false
     */
    @Override
    public boolean existsUsername(String username) {
        return memberRepository.existsByUsername(username);
    }
}
