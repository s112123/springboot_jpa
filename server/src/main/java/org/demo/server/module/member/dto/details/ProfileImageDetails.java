package org.demo.server.module.member.dto.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.entity.ProfileImage;

@Data
@Builder
public class ProfileImageDetails {

    @JsonIgnore
    private Long profileImageId;
    private String originalFileName;
    private String savedFileName;

    /**
     * (DTO → Entity) ProfileImageDetails → ProfileImage
     *
     * @return ProfileImage
     */
    public ProfileImage toProfileImage() {
        return ProfileImage.builder()
                .profileImageId(this.profileImageId)
                .originalFileName(this.originalFileName)
                .savedFileName(this.savedFileName)
                .build();
    }
}
