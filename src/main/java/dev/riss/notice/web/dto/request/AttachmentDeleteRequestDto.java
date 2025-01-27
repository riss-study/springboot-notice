package dev.riss.notice.web.dto.request;

import lombok.Data;

import java.util.ArrayList;

@Data
public class AttachmentDeleteRequestDto {
    private ArrayList<Long> attachmentIdList;
}
