package dev.riss.notice.web;

import dev.riss.notice.service.NoticeService;
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
     * 게시글 첨부파일 등록 API
     * (일단 편의 상 첨부파일 DB + storage 업로드 한꺼번에 할 예정)
     * @param files
     * @return
     */
    @PostMapping("/{notice_uid}/files")
    public ResponseDto uploadNoticeAttachment(@RequestParam("files") MultipartFile[] files,
    @PathVariable("notice_uid") long noticeUid) {
        return noticeService.uploadNoticeAttachment(files, noticeUid);
    }

    /*
     * 게시글 수정 API
     * */


    /*
     * 게시글 삭제 API
     * */

    /*
    *
    * 전체 조회 API
    * Todo: List<{ uid, title, createdAt }> 형태로 DTO 수정 (현재는 하나의 게시글 조회 API 용 DTO 를 List 로 응답)
    * */
    @GetMapping("/all")
    public ResponseDto findAll(@RequestParam(required = false, defaultValue = "0", name = "page") int page) {
        return noticeService.findAll(page);
    }

    /**
     * 단건 조회 API
     * Todo: views count plus
     * @param noticeUid
     * @return
     */
    @GetMapping("/{notice_uid}")
    public ResponseDto findById(@PathVariable("notice_uid") Long noticeUid) {
        return noticeService.findById(noticeUid);
    }
}
