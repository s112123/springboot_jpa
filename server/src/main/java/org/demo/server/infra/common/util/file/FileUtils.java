package org.demo.server.infra.common.util.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtils {

    private final FileProperties fileProperties;

    public FileDetails saveFile(MultipartFile multipartFile, UploadDirectory uploadDirectory) {
        String uploadDirectoryPath = fileProperties.getUploadDirectory(uploadDirectory);

        // 파일이 있는지 확인
        if (isAttached(multipartFile))
            throw new IllegalArgumentException("파일을 첨부해주세요");
        // 업로드 폴더 확인
        makeIfNotExistsUploadDirectory(uploadDirectoryPath);

        // 파일 정보
        FileDetails fileDetails = new FileDetails(multipartFile);

        // 파일 저장
        try {
            multipartFile.transferTo(new File(uploadDirectoryPath, fileDetails.getSaveFileName()));
            return fileDetails;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 서버에 파일 저장
     *
     * @param multipartFile 저장할 파일
     * @return 저장된 파일 정보
     */
//    public FileDetails saveFile(MultipartFile multipartFile) {
//        // 파일이 있는지 확인
//        if (isAttached(multipartFile))
//            throw new IllegalArgumentException("파일을 첨부해주세요");
//        // 업로드 폴더 확인
//        makeIfNotExistsUploadDirectory(uploadRootDirPath);
//
//        // 파일 정보
//        FileDetails fileDetails = new FileDetails(multipartFile);
//
//        // 파일 저장
//        try {
//            multipartFile.transferTo(new File(uploadRootDirPath, fileDetails.getSaveFileName()));
//            return fileDetails;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
}
