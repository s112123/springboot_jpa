package org.demo.server.module.notice.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.service.publisher.MessagePublisher;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.demo.server.module.notice.dto.details.NoticeDetails;
import org.demo.server.module.notice.dto.request.NoticeCreateRequest;
import org.demo.server.module.notice.entity.Notice;
import org.demo.server.module.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberFinder memberFinder;
    private final MessagePublisher messagePublisher;

    /**
     * 공지 사항 등록
     *
     * @param request 등록할 공지사항 정보
     * @return 등록된 공지사항
     */
    @Transactional
    public NoticeDetails save(NoticeCreateRequest request) {
        // 작성자 조회
        Member writer = memberFinder.getMemberById(request.getWriterId());

        // 공지사항 등록
        Notice notice = Notice.builder()
                .content(request.getContent())
                .writer(writer)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 공지 알림 전송
        messagePublisher.publishNotice(writer.getMemberId(), savedNotice.getId());
        return new NoticeDetails(savedNotice);
    }

    /**
     * 공지 목록
     *
     * @param page 조회할 페이지
     * @return 공지 목록
     */
    @Transactional(readOnly = true)
    public Page<NoticeDetails> findAll(int page) {
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notice> findNotices = noticeRepository.findAll(pageable);
        Page<NoticeDetails> noticeDetailsList = findNotices.map(notice -> new NoticeDetails(notice));
        return noticeDetailsList;
    }
}
