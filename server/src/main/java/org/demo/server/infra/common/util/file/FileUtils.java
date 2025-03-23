package org.demo.server.infra.common.util.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileUtils {

    private final FileProperties fileProperties;

    /**
     * 서버에 파일 저장
     *
     * @param multipartFile 저장할 파일
     * @return 저장된 파일 정보
     */
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
            multipartFile.transferTo(new File(uploadDirectoryPath, fileDetails.getSavedFileName()));
            return fileDetails;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 서버에서 하나의 파일 삭제
     *
     * @param fileName 삭제할 파일 이름
     * @param uploadDirectory 삭제할 파일이 있는 폴더 경로
     * @return 삭제된 경우, true 를 반환하고 그렇지 않으면 false 반환
     */
    public boolean deleteFile(String fileName, UploadDirectory uploadDirectory) {
        File deletedFile = new File(getUploadDirectory(uploadDirectory) + fileName);
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
    public void deleteFiles(List<String> fileNames, UploadDirectory uploadDirectory) {
        for (String fileName : fileNames) {
            deleteFile(fileName, uploadDirectory);
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
}
