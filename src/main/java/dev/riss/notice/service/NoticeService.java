package dev.riss.notice.service;

import dev.riss.notice.domain.notice.Attachment;
import dev.riss.notice.domain.notice.AttachmentRepository;
import dev.riss.notice.domain.notice.Notice;
import dev.riss.notice.domain.notice.NoticeRepository;
import dev.riss.notice.util.ResponseUtil;
import dev.riss.notice.web.dto.UidDto;
import dev.riss.notice.web.dto.request.NoticeRequestDto;
import dev.riss.notice.web.dto.response.NoticeAttachmentDto;
import dev.riss.notice.web.dto.response.NoticeRetrieveDto;
import dev.riss.notice.web.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AttachmentRepository attachmentRepository;

    private final String SERVER_HTTP_URL = "http://localhost:8080";
    private final String NOTICE_ATTACHMENT_URL = "uploads";

    private final int PAGE_SIZE = 10;

    public ResponseDto<UidDto> createNotice(NoticeRequestDto noticeDto) {
        Notice notice = Notice
                .builder()
                .title(noticeDto.getTitle())
                .content(noticeDto.getContent())
                .startAt(noticeDto.getStartAt())
                .views(0L)
                .author(noticeDto.getAuthor())
                .endAt(noticeDto.getEndAt())
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        UidDto noticeUidDto = UidDto.builder().uid(savedNotice.getUid()).build();
        return ResponseUtil.SUCCESS("", noticeUidDto);
    }

    public ResponseDto uploadNoticeAttachment(MultipartFile[] files, Long noticeUid) {

        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new RuntimeException("첨부파일을 업로드하려는 게시글이 존재하지 않습니다."));

        List<Attachment> attachmentList = new ArrayList<>();

        for (MultipartFile file : files) {
            String originFileName = file.getOriginalFilename();
            String newFileName = Attachment.toNewFileName(originFileName, findNotice.getUid());

            Attachment attachment = Attachment.builder()
                    .originFileName(originFileName)
                    .newFileName(newFileName)
                    .path(NOTICE_ATTACHMENT_URL)
                    .notice(findNotice)
                    .build();

            attachmentList.add(attachment);
            storeFile(file, newFileName);
        }
        attachmentRepository.saveAll(attachmentList);
        return ResponseUtil.SUCCESS("", null);
    }

    private boolean storeFile(MultipartFile file, String newFileName) {
        try {
            Path targetLocation = Paths.get(NOTICE_ATTACHMENT_URL + "/" + newFileName).toAbsolutePath().normalize();
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), e);
        }

    }

    @Transactional(readOnly = true)
    public ResponseDto<List<NoticeRetrieveDto>> findAll(int pageNo) {
        PageRequest pageRequest = PageRequest.of(pageNo, PAGE_SIZE);
        Page<Notice> page = noticeRepository.findAllByEndAtAfter(LocalDateTime.now(), pageRequest);

        if (!page.hasContent()) return ResponseUtil.FAILURE("데이터가 없습니다.", null);
        List<Notice> content = page.getContent();

        List<NoticeRetrieveDto> resultData = content.stream().map(Notice::toNoticeRetrieveDto).collect(Collectors.toList());

        return ResponseUtil.SUCCESS("", resultData);
    }

    @Transactional(readOnly = true)
    public ResponseDto<NoticeRetrieveDto> findById(Long noticeUid) {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        List<NoticeAttachmentDto> attachmentDtoList = attachmentRepository.findAllByNoticeUid(noticeUid)
                .stream().map(attachment -> attachment.toDto(SERVER_HTTP_URL))
                .collect(Collectors.toList());

        NoticeRetrieveDto resultData = NoticeRetrieveDto.builder()
                .uid(findNotice.getUid())
                .title(findNotice.getTitle())
                .content(findNotice.getContent())
                .createdAt(findNotice.getCreatedAt())
                .views(findNotice.getViews())
                .author(findNotice.getAuthor())
                .noticeAttachmentDtoList(attachmentDtoList)
                .build();

        return ResponseUtil.SUCCESS("", resultData);
    }
}
