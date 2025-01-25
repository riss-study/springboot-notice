package dev.riss.notice.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeSimpleRetrieveDto {

    private Long uid;
    private String title;
    private String createdAt;
}
