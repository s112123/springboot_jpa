package org.demo.server.module.member.service.base;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.exception.NotFoundException;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFinder {

    private final MemberRepository memberRepository;

    /**
     * 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return Member Entity
     */
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다"));
    }

    /**
     * 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return Member Entity
     */
    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다"));
    }

    /**
     * 회원 정보 조회
     *
     * @param email 조회 할 회원의 email
     * @return 조회된 회원 엔티티
     */
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다"));
    }
}
