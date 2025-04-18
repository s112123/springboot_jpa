package org.demo.server.infra.common.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class DeleteScheduler {

    private final FileUtils fileUtils;
    private final MemberFinder memberFinder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DeleteScheduler(
            FileUtils fileUtils,
            MemberFinder memberFinder,
            @Qualifier("redisTemplate02") RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.fileUtils = fileUtils;
        this.memberFinder = memberFinder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 12시간 마다 Redis 에 캐시된 알림 메세지가 7일 전이면 삭제
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 12)
    public void deleteCachedNotifications() {
        // 전체 회원의 ID
        Set<Long> memberIds = memberFinder.getAllMembers().stream()
                .map(member -> member.getMemberId())
                .collect(Collectors.toSet());

        // 캐시된 알림 메세지 삭제
        for (Long memberId : memberIds) {
            String redisKey = "notification:consumer:" + memberId;
            Set<Object> notifications = redisTemplate.opsForSet().members(redisKey);

            if (notifications.isEmpty()) {
                continue;
            }

            for (Object notification : notifications) {
                MessageDetails messageDetails = objectMapper.convertValue(notification, MessageDetails.class);
                // 7일 전
                if (messageDetails.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                    redisTemplate.opsForSet().remove(redisKey, notification);
                }
            }
        }
    }

    /**
     * 24시간 마다 temp 폴더 내 폴더가 2일 전이면 삭제
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void deleteTempDirectories() {
        // temps 폴더
        Path tempDirectory = Path.of(fileUtils.getUploadDirectory(UploadDirectory.TEMPS));
        if (!Files.exists(tempDirectory)) {
            return;
        }

        try (Stream<Path> paths = Files.list(tempDirectory)) {
            // temps 폴더의 내부 순회
            paths.forEach(path -> {
                try {
                    // 현재 시간에서 1일 전 시간
                    // 1분 전인 경우 → LocalDateTime.now().minusMinutes(1L);
                    LocalDateTime beforeOneDay = LocalDateTime.now().minusDays(2L);
                    // 파일의 마지막 수정 날짜
                    FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                    // FileTime → LocalDateTime
                    LocalDateTime modifiedTime =
                            LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());

                    // FileTime 이 1일 전인 경우 → 디렉토리의 수정날짜가 1일 전인 경우
                    if (modifiedTime.isBefore(beforeOneDay)) {
                        deleteTempFiles(path);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 폴더는 안에 있는 파일이 모두 삭제되고 비어있는 폴더만 삭제할 수 있다
     * 제일 깊은 경로에 있는 파일 -> 폴더 순으로 삭제해야 한다
     *
     * @param path 내부를 순환할 폴더
     */
    private void deleteTempFiles(Path path) {
        if (Files.exists(path)) {
            // walk() 는 하위 디렉토리까지 재귀적으로 탐색하고 list() 는 현재 폴더만 탐색한다
            try (Stream<Path> walk = Files.walk(path)) {
                // 깊은 경로부터 탐색 → Comparator.reverseOrder()
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("DeletedFileScheduler Error, p={}", p.toAbsolutePath());
                        }
                    });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
