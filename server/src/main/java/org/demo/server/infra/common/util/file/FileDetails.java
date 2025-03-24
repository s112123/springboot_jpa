package org.demo.server.infra.common.util.file;


import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
public class FileDetails {

    private final String originalFileName;
    private final String savedFileName;
    private final String path;

    public FileDetails(MultipartFile multipartFile, String path) {
        this.originalFileName = multipartFile.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.indexOf(".") + 1);
        this.savedFileName = UUID.randomUUID() + "." + extension;
        this.path = path;
    }
}
