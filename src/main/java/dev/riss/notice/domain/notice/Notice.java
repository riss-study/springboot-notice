package dev.riss.notice.domain.notice;

import dev.riss.notice.domain.BaseEntity;
import dev.riss.notice.util.DateUtil;
import dev.riss.notice.web.dto.request.NoticeRequestDto;
import dev.riss.notice.web.dto.response.NoticeAttachmentDto;
import dev.riss.notice.web.dto.response.NoticeRetrieveDto;
import dev.riss.notice.web.dto.response.NoticeSimpleRetrieveDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long views;
    
    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @OneToMany(mappedBy = "notice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Attachment> attachmentList = new ArrayList<>();

    public NoticeRetrieveDto toNoticeRetrieveDto(List<NoticeAttachmentDto> noticeAttachmentDtoList) {
        return NoticeRetrieveDto.builder()
                .uid(this.getUid())
                .title(this.getTitle())
                .content(this.getContent())
                .createdAt(DateUtil.toString(this.getCreatedAt()))
                .views(this.getViews())
                .author(this.getAuthor())
                .noticeAttachmentDtoList(noticeAttachmentDtoList)
                .build();
    }

    public NoticeSimpleRetrieveDto toNoticeSimpleRetrieveDto() {
        return NoticeSimpleRetrieveDto.builder()
                .uid(this.getUid())
                .title(this.getTitle())
                .createdAt(DateUtil.toString(this.getCreatedAt()))
                .build();
    }

    public void update(NoticeRequestDto noticeDto) {
        this.title = noticeDto.getTitle();
        this.content = noticeDto.getContent();
        this.author = noticeDto.getAuthor();
        this.startAt = noticeDto.getStartAt();
        this.endAt = noticeDto.getEndAt();
    }
}
