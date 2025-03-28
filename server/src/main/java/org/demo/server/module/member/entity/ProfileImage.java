package org.demo.server.module.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.member.dto.details.ProfileImageDetails;

@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "saved_file_name", nullable = false)
    private String savedFileName;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private ProfileImage(Builder builder) {
        this.profileImageId = builder.profileImageId;
        this.originalFileName = builder.originalFileName;
        this.savedFileName = builder.savedFileName;
        this.member = builder.member;
    }

    /**
     * 기존 프로필 이미지 파일 이름 변경
     *
     * @param originalFileName 프로필 파일 이름
     */
    public void updateOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    /**
     * 서버에 저장된 프로필 이미지 파일 이름 변경
     *
     * @param savedFileName 프로필 파일 이름
     */
    public void updateSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }

    /**
     * ProfileImage Entity 를 받아서 ProfileImageDetails DTO 를 생성한다 (Entity → DTO)
     *
     * @return ProfileImageDetails DTO 를 반환한다
     */
    public ProfileImageDetails toDetails() {
        return ProfileImageDetails.builder()
                .profileImageId(this.getProfileImageId())
                .originalFileName(this.getOriginalFileName())
                .savedFileName(this.getSavedFileName())
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
        private Member member;

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

        public Builder member(Member member) {
            this.member = member;
            return this;
        }

        public ProfileImage build() {
            return new ProfileImage(this);
        }
    }
}
