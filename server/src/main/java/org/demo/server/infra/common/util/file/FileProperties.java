package org.demo.server.infra.common.util.file;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileProperties {

    // application.properties 파일에서 "file.upload.root" 값이 들어온다
    private String root;
    // application.properties 파일에서 "file.upload.directories" 값이 들어온다
    private Map<String, String> directories = new HashMap<>();

    /**
     * 첨부된 파일을 저장할 경로
     *
     * @param uploadDirectory 저장할 폴더 이름
     * @return 파일이 저장되는 경로
     */
    public String getUploadDirectory(UploadDirectory uploadDirectory) {
        return root + File.separator + directories.get(uploadDirectory.name().toLowerCase());
    }

    /**
     * 첨부된 파일을 저장할 경로
     *
     * @param uploadDirectory 저장할 폴더 이름
     * @param subDirectories uploadDirectory 로 설정된 폴더의 자식 폴더
     * @return 파일이 저장되는 경로
     */
    public String getUploadDirectory(UploadDirectory uploadDirectory, String... subDirectories) {
        StringBuilder sb = new StringBuilder();
        sb.append(root + File.separator);
        sb.append(uploadDirectory.name().toLowerCase() + File.separator);
        for (String subDirectory : subDirectories) {
            sb.append(subDirectory + File.separator);
        }
        return sb.toString();
    }
}
