package org.demo.server.infra.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.util.file.FileUtils;
import org.demo.server.infra.common.util.file.UploadDirectory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteFileScheduler {

    private final FileUtils fileUtils;

    /**
     * temp 폴더 내 폴더가 1일 전이면 삭제
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
                    LocalDateTime beforeOneDay = LocalDateTime.now().minusDays(1L);
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
