package dev.riss.notice.domain.notice;

import dev.riss.notice.domain.BaseEntity;
import dev.riss.notice.web.dto.response.NoticeRetrieveDto;
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

    public static NoticeRetrieveDto toNoticeRetrieveDto(Notice notice) {
        return NoticeRetrieveDto.builder()
                .uid(notice.getUid())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .views(notice.getViews())
                .author(notice.getAuthor())
                .build();
    }
}
