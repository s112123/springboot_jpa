package org.demo.server.module.notice.repository;

import org.demo.server.module.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /**
     * 공지사항 목록 조회
     * 
     * @param pageable 페이지네이션
     * @return 공지사항 목록
     */
    Page<Notice> findAll(Pageable pageable);
}
