package dev.riss.notice.domain.notice;

import dev.riss.notice.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "공지사항 제목")
    private String title;
    
    @Column(nullable = false, columnDefinition = "공지사항 내용")
    private String content;

    @Column(columnDefinition = "공지사항 조회수")
    private Long views;
    
    @Column(columnDefinition = "공지사항 글쓴이")
    private String author;

    @Column(nullable = false, columnDefinition = "공지 시작 일시")
    private LocalDateTime startAt;

    @Column(nullable = false, columnDefinition = "공지 종료 일시")
    private LocalDateTime endAt;

    @OneToMany(mappedBy = "notice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Attachment> attachmentList = new ArrayList<>();
}
