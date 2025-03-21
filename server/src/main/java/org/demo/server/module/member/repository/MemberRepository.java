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
     * email 을 사용하여 회원 정보 조회
     *
     * @param email 회원의 email
     * @return 회원 정보
     */
    Optional<Member> findByEmail(String email);
}
