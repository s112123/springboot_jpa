package org.demo.server.infra.common.util.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtils {

    private final FileProperties fileProperties;

    /**
     * 서버에 파일 저장
     *
     * @param multipartFile 저장할 파일
     * @param uploadDirectory 저장 폴더
     * @return 저장된 파일 정보
     */
    public FileDetails saveFile(MultipartFile multipartFile, UploadDirectory uploadDirectory) {
        return saveFile(multipartFile, uploadDirectory, "");
    }

    /**
     * 서버에 파일 저장
     *
     * @param multipartFile 저장할 파일
     * @param uploadDirectory 저장 폴더
     * @param subDirectories uploadDirectory 폴더의 자식 폴더
     * @return 저장된 파일 정보
     */
    public FileDetails saveFile(
            MultipartFile multipartFile, UploadDirectory uploadDirectory, String... subDirectories
    ) {
        // 저장할 폴더 경로
        String uploadDirectoryPath = fileProperties.getUploadDirectory(uploadDirectory, subDirectories);

        // 파일이 있는지 확인
        if (isAttached(multipartFile))
            throw new IllegalArgumentException("파일을 첨부해주세요");
        // 업로드 폴더 확인
        makeIfNotExistsUploadDirectory(uploadDirectoryPath);

        // 파일 정보
        FileDetails fileDetails = new FileDetails(multipartFile);

        // 파일 저장
        try {
            multipartFile.transferTo(new File(uploadDirectoryPath, fileDetails.getSavedFileName()));
            return fileDetails;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 파일 이동
     *
     * @param fileName 이동할 파일 이름
     * @param sourceDirectoryPath 원본 폴더 경로
     * @param targetDirectoryPath 파일을 이동할 폴더 경로
     */
    public void moveFile(String fileName, String sourceDirectoryPath, String targetDirectoryPath) {
        // 원본 파일
        File sourceFile = new File(sourceDirectoryPath + fileName);
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("원본 파일이 없습니다");
        }

        // 이동 파일
        makeIfNotExistsUploadDirectory(targetDirectoryPath);
        File targetFile = new File(targetDirectoryPath + fileName);

        try {
            // 파일 이동
            // 파일을 복사할 때는 move() 대신 copy() 를 동일하게 사용하면 된다
            Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("source={} → target={}", sourceFile.getAbsolutePath(), targetFile.getAbsoluteFile());
        } catch (IOException e) {
            log.info("FileUtils.moveFile → 원본 파일이 없습니다");
        }
    }

    /**
     * 서버에서 하나의 파일 삭제
     *
     * @param fileName 삭제할 파일 이름
     * @param uploadDirectory 삭제할 파일이 있는 폴더 경로
     * @return 삭제된 경우, true 를 반환하고 그렇지 않으면 false 반환
     */
    public boolean deleteFile(String fileName, UploadDirectory uploadDirectory, String... subDirectories) {
        File deletedFile = new File(getUploadDirectory(uploadDirectory, subDirectories) + fileName);
        if (deletedFile.exists()) {
            deletedFile.delete();
            return true;
        }
        return false;
    }

    /**
     * 서버에서 다량의 파일 삭제
     *
     * @param fileNames 삭제할 파일 이름 목록
     * @param uploadDirectory 삭제할 파일이 있는 폴더 경로
     */
    public void deleteFiles(List<String> fileNames, UploadDirectory uploadDirectory, String... subDirectories) {
        for (String fileName : fileNames) {
            deleteFile(fileName, uploadDirectory, subDirectories);
        }
    }

    /**
     * 파일이 첨부되었는지 확인
     *
     * @param multipartFile 업로드 파일
     * @return 업로드 파일이 존재하는지 여부
     */
    private boolean isAttached(MultipartFile multipartFile) {
        return multipartFile == null || multipartFile.isEmpty();
    }

    /**
     * 업로드 디렉토리가 없으면 디렉토리 생성
     *
     * @param uploadDirPath 업로드 디렉토리 경로
     */
    private void makeIfNotExistsUploadDirectory(String uploadDirPath) {
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    /**
     * 업로드 디렉토리 경로 반환
     * 
     * @param uploadDirectory 경로를 반환할 디렉토리 이름 
     * @return 업로드 디렉토리 경로
     */
    public String getUploadDirectory(UploadDirectory uploadDirectory) {
        return fileProperties.getUploadDirectory(uploadDirectory);
    }

    /**
     * 업로드 디렉토리 경로 반환
     *
     * @param uploadDirectory 경로를 반환할 디렉토리 이름
     * @param subDirectory uploadDirectory 의 하위 폴더
     * @return 업로드 디렉토리 경로
     */
    public String getUploadDirectory(UploadDirectory uploadDirectory, String... subDirectory) {
        return fileProperties.getUploadDirectory(uploadDirectory, subDirectory);
    }
}
