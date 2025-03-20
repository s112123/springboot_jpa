package org.demo.server.module.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.member.dto.details.ProfileImageDetails;

@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor(force = true)
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private final Long profileImageId;

    @Column(name = "original_file_name", nullable = false)
    private final String originalFileName;

    @Column(name = "saved_file_name", nullable = false)
    private final String savedFileName;

    @Column(name = "extension", nullable = false)
    private final String extension;

    private ProfileImage(Builder builder) {
        this.profileImageId = builder.profileImageId;
        this.originalFileName = builder.originalFileName;
        this.savedFileName = builder.savedFileName;
        this.extension = builder.extension;
    }

    /**
     * ProfileImage Entity 를 받아서 ProfileImageDetails DTO 를 생성한다
     *
     * @param profileImage ProfileImageDetails DTO 로 변경할 ProfileImage 엔티티
     * @return ProfileImageDetails DTO 를 반환한다
     */
    public static ProfileImageDetails toProfileImageDetails(ProfileImage profileImage) {
        return ProfileImageDetails.builder()
                .profileImageId(profileImage.getProfileImageId())
                .originalFileName(profileImage.getOriginalFileName())
                .savedFileName(profileImage.getSavedFileName())
                .extension(profileImage.getExtension())
                .build();
    }

    /**
     * ProfileImage Entity 를 생성하는 Builder 생성
     *
     * @return Builder 객체 반환
     */
    public static Builder builder() {
        return new Builder();
    }

    // ProfileImage.Builder()
    public static class Builder {

        private Long profileImageId;
        private String originalFileName;
        private String savedFileName;
        private String extension;

        public Builder profileImageId(Long profileImageId) {
            this.profileImageId = profileImageId;
            return this;
        }

        public Builder originalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
            return this;
        }

        public Builder savedFileName(String savedFileName) {
            this.savedFileName = savedFileName;
            return this;
        }

        public Builder extension(String extension) {
            this.extension = extension;
            return this;
        }

        public ProfileImage build() {
            return new ProfileImage(this);
        }
    }
}
