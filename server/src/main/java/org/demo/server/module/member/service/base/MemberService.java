package org.demo.server.module.member.service.base;

import org.demo.server.module.member.dto.details.MemberDetails;
import org.demo.server.module.member.dto.form.MemberSaveForm;
import org.demo.server.module.member.dto.form.MemberUpdateForm;
import org.demo.server.module.member.entity.Role;
import org.springframework.data.domain.Page;

public interface MemberService {

    /**
     * 회원 등록
     *
     * @param form 회원 가입 시, 사용자에게서 입력받은 회원 가입 정보
     * @return 회원 가입 후, 가입된 회원 정보
     */
    MemberDetails save(MemberSaveForm form);

    /**
     * memberId 로 회원 정보 조회
     *
     * @param memberId 조회 할 회원의 memberId
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    MemberDetails findById(Long memberId);

    /**
     * username 로 회원 정보 조회
     *
     * @param username 조회 할 회원의 username
     * @return 조회된 회원 정보를 반환하고 회원이 존재하지 않으면 NotFoundMemberException
     */
    MemberDetails findByUsername(String username);

    /**
     * 회원 목록 조회
     *
     * @return createdAt 을 기준으로 내림차순으로 정렬한 회원 목록
     */
    Page<MemberDetails> findAll(int page);

    /**
     * 회원 정보 수정
     *
     * @param username 회원 정보를 수정할 회원의 닉네임
     * @param updateRequest 수정할 회원 정보 내용
     * @return 수정 완료된 내용
     */
    MemberDetails update(String username, MemberUpdateForm updateRequest);

    /**
     * 회원의 비밀번호를 임시 비밀번호로 변경
     *
     * @param email 회원 이메일
     * @param tempPassword 임시 비밀번호
     */
    void updateTempPasswordByEmail(String email, String tempPassword);

    /**
     * 회원의 권한 변경
     *
     * @param memberId 회원 식별자
     * @param role 변경할 권한
     */
    void updateRole(Long memberId, Role role);

    /**
     * email 로 회원 정보 삭제
     *
     * @param email 삭제 할 회원의 email
     */
    void deleteByEmail(String email);

    /**
     * 이메일이 존재하는지 여부
     *
     * @param email 존재하는지 확인 할 이메일
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsEmail(String email);

    /**
     * 닉네임이 존재하는지 여부
     *
     * @param username 존재하는지 확인 할 닉네임
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsUsername(String username);
}
