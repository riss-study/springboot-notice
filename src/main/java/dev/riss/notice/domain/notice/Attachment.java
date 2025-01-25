package dev.riss.notice.domain.notice;

import dev.riss.notice.web.dto.response.NoticeAttachmentDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    private String originFileName;
    private String newFileName;

    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    public void addNotice(Notice notice) {
        this.notice = notice;
        notice.getAttachmentList().add(this);
    }

    public static String toNewFileName(String fileName, long noticeId) {
        String uuid = UUID.randomUUID().toString();
        return noticeId + "_" + uuid + "." + getFileExtension(fileName);
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
        return fileName.substring(dotIndex + 1);
    }

    @Builder
    public Attachment(String originFileName, String newFileName, String path, Notice notice) {
        this.originFileName = originFileName;
        this.newFileName = newFileName;
        this.path = path;
        addNotice(notice);
    }

    public NoticeAttachmentDto toDto (String url) {
        return NoticeAttachmentDto
                .builder()
                .uid(uid)
                .originFileName(originFileName)
                .fileUrl(url + "/" + path + "/" +  newFileName)
                .build();
    }
}
