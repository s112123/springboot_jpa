package org.demo.server.infra.common.util.file;


import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
public class FileDetails {

    private final String originalFileName;
    private final String saveFileName;
    private final String extension;

    public FileDetails(MultipartFile multipartFile) {
        this.originalFileName = multipartFile.getOriginalFilename();
        this.extension = originalFileName.substring(originalFileName.indexOf(".") + 1);
        this.saveFileName = UUID.randomUUID() + "." + extension;
    }
}
