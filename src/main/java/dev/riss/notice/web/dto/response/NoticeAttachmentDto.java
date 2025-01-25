package dev.riss.notice.web.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoticeAttachmentDto {

    public Long uid;
    public String originFileName;
    public String fileUrl;
}
