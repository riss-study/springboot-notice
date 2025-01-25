package dev.riss.notice.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoticeRetrieveDto {

    private Long uid;
    private String title;
    private String content;

    private String createdAt;
    private Long views;
    private String author;

    private List<NoticeAttachmentDto> noticeAttachmentDtoList;
}