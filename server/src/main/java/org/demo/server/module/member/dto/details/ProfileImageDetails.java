package org.demo.server.module.member.dto.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileImageDetails {

    @JsonIgnore
    private Long profileImageId;
    private String originalFileName;
    private String savedFileName;
    private String extension;
}
