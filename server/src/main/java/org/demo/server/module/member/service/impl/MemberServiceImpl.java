package org.demo.server.module.member.service.impl;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.dto.PagedListResponse;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.request.MemberSaveRequest;
import org.demo.server.module.member.dto.response.MemberResponse;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.entity.ProfileImage;
import org.demo.server.module.member.entity.Role;
import org.demo.server.module.member.exception.DuplicatedEmailException;
import org.demo.server.module.member.repository.MemberRepository;
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

    /**
     * 회원 등록
     *
     * @param memberSaveRequest 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    @Override
    public MemberResponse save(MemberSaveRequest memberSaveRequest) {
        // 이메일 중복 체크
        if (existsEmail(memberSaveRequest.getEmail())) {
            throw new DuplicatedEmailException("이미 존재하는 이메일입니다");
        }

        // 기본 프로필 이미지 등록
        ProfileImage profileImage = ProfileImage.builder()
                .originalFileName("original")
                .savedFileName("saved")
                .extension("png")
                .build();

        // 회원 등록
        Member member = Member.builder()
                .email(memberSaveRequest.getEmail())
                .password(passwordEncoder.encode(memberSaveRequest.getPassword()))
                .username("user-" + UUID.randomUUID())
                .role(Role.USER)
                .updatedAt(LocalDateTime.now())
                .profileImage(profileImage)
                .build();
        Member savedMember = memberRepository.save(member);

        // Entity → Details → Response
        MemberDetails memberDetails = Member.toMemberDetails(savedMember);
        return MemberDetails.toMemberResponse(memberDetails);
    }

    /**
     * memberId 로 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @Override
    @Transactional(readOnly = true)
    public MemberResponse findById(Long memberId) {
        Member findMember = getMemberById(memberId);
        MemberDetails memberDetails = Member.toMemberDetails(findMember);
        return MemberDetails.toMemberResponse(memberDetails);
    }

    /**
     * username 로 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    @Override
    @Transactional(readOnly = true)
    public MemberResponse findByUsername(String username) {
        Member findMember = getMemberByUsername(username);
        MemberDetails memberDetails = Member.toMemberDetails(findMember);
        return MemberDetails.toMemberResponse(memberDetails);
    }

    /**
     * 회원 목록 조회
     *
     * @return createdAt 을 기준으로 내림차순으로 정렬한 회원 목록과 회원 수
     */
    @Override
    @Transactional(readOnly = true)
    public PagedListResponse<MemberResponse> findAll(int page) {
        // 요청 페이지 번호가 1보다 작으면 에러가 발생한다
        if (page < 1) {
            throw new IllegalArgumentException("존재하지 않는 페이지입니다");
        }

        // 회원 목록 조회
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MemberResponse> findMembers = memberRepository.findAll(pageable)
                .map(member -> Member.toMemberDetailsWithoutProfileImage(member))
                .map(memberDetails -> MemberDetails.toMemberResponse(memberDetails));
        return new PagedListResponse<>(findMembers);
    }

    /**
     * memberId 로 회원 정보 삭제
     *
     * @param memberId 조회 할 회원의 memberId
     */
    @Override
    public void deleteById(Long memberId) {
        getMemberById(memberId);
        memberRepository.deleteById(memberId);
    }

    /**
     * memberId 로 회원 정보를 조회하는 Private 메서드
     *
     * @param memberId 조회 할 회원의 memberId
     * @return Member Entity
     */
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다"));
    }

    /**
     * username 로 회원 정보를 조회하는 Private 메서드
     *
     * @param username 조회 할 회원의 username
     * @return Member Entity
     */
    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다"));
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
}
