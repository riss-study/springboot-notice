package dev.riss.notice.web;

import dev.riss.notice.service.NoticeService;
import dev.riss.notice.web.dto.request.AttachmentDeleteRequestDto;
import dev.riss.notice.web.dto.request.NoticeRequestDto;
import dev.riss.notice.web.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/notice")
public class NoticeController {

    private final NoticeService noticeService;


    /**
     * 게시글 DB 등록 API
     * @param noticeDto
     * @return
     */
    @PostMapping
    public ResponseDto createNotice(@RequestBody NoticeRequestDto noticeDto) {
        return noticeService.createNotice(noticeDto);
    }

    /**
     * 게시글 수정 API
     * @param noticeUid
     * @param noticeDto
     * @return
     */
    @PutMapping("/{uid}")
    public ResponseDto updateNotice(@PathVariable("uid") Long noticeUid,
                                    @RequestBody NoticeRequestDto noticeDto) {
        return noticeService.updateNotice(noticeUid, noticeDto);
    }

    /**
     * 게시글 삭제 API
     * @param noticeUid
     * @return
     */
    @DeleteMapping("/{uid}")
    public ResponseDto deleteNotice(@PathVariable("uid") Long noticeUid) {
        return noticeService.deleteNotice(noticeUid);
    }

    /**
     * 게시글 전체 조회 API
     * @param page
     * @return
     */
    @GetMapping("/all")
    public ResponseDto findAll(@RequestParam(required = false, defaultValue = "0", name = "page") int page) {
        return noticeService.findAll(page);
    }

    /**
     * 게시글 단건 조회 API
     * @param noticeUid
     * @return
     */
    @GetMapping("/{uid}")
    public ResponseDto findById(@PathVariable("uid") Long noticeUid) {
        return noticeService.findById(noticeUid);
    }

    /**
     * 게시글 첨부파일 등록 API
     * (일단 편의 상 첨부파일 DB + storage 업로드 한꺼번에 할 예정)
     * @param attachments
     * @return
     */
    @PostMapping("/{uid}/attachment")
    public ResponseDto uploadNoticeAttachment(@RequestParam("attachments") MultipartFile[] attachments,
                                              @PathVariable("uid") Long noticeUid) {
        return noticeService.uploadNoticeAttachment(attachments, noticeUid);
    }

    /**
     * 게시글 첨부파일 삭제 API
     * @param noticeUid
     * @param attachmentDeleteRequestDto
     * @return
     */
    @PostMapping("/{uid}/attachment/bulk-delete")
    public ResponseDto deleteNoticeAttachment(@PathVariable("uid") Long noticeUid,
                                              @RequestBody AttachmentDeleteRequestDto attachmentDeleteRequestDto) {
        return noticeService.deleteNoticeAttachment(noticeUid, attachmentDeleteRequestDto);
    }
}
