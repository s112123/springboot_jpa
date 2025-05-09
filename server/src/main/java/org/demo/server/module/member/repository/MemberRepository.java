package org.demo.server.module.member.repository;

import org.demo.server.module.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일이 존재하는지 여부
     *
     * @param email 존재하는지 확인 할 이메일
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임이 존재하는지 여부
     *
     * @param username 존재하는지 확인 할 닉네임
     * @return 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByUsername(String username);

    /**
     * 회원 정보 조회
     *
     * @param email 회원의 email
     * @return 회원 정보
     */
    Optional<Member> findByEmail(String email);

    /**
     * 회원 정보 조회
     *
     * @param username 회원의 닉네임 (username)
     * @return 회원 정보
     */
    Optional<Member> findByUsername(String username);

    /**
     * 회원 삭제
     *
     * @param email 삭제할 회원의 email
     */
    void deleteByEmail(String email);
}
