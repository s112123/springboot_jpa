package org.demo.server.module.member.service.base;

import org.demo.server.infra.common.dto.PagedListResponse;
import org.demo.server.module.member.dto.request.MemberSaveRequest;
import org.demo.server.module.member.dto.response.MemberResponse;

public interface MemberService {

    /**
     * 회원 등록
     *
     * @param form 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    MemberResponse save(MemberSaveRequest form);

    /**
     * memberId 로 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    MemberResponse findById(Long memberId);

    /**
     * username 로 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    MemberResponse findByUsername(String username);

    /**
     * 회원 목록 조회
     *
     * @return createdAt 을 기준으로 내림차순으로 정렬한 회원 목록
     */
    PagedListResponse<MemberResponse> findAll(int page);

    /**
     * memberId 로 회원 정보 삭제
     *
     * @param memberId 삭제 할 회원의 memberId
     */
    void deleteById(Long memberId);

    /**
     * 이메일이 존재하는지 여부
     *
     * @param email 존재하는지 확인 할 이메일
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsEmail(String email);
}
