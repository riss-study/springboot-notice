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
import dev.riss.notice.web.dto.response.NoticeSimpleRetrieveDto;
import dev.riss.notice.web.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Long, Long> viewCountCache = new ConcurrentHashMap<>();

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
        return ResponseUtil.success(noticeUidDto);
    }


    @Transactional(readOnly = true)
    public ResponseDto<List<NoticeSimpleRetrieveDto>> findAll(int pageNo) {
        PageRequest pageRequest = PageRequest.of(pageNo, PAGE_SIZE);
        Page<Notice> page = noticeRepository.findAllByEndAtAfter(LocalDateTime.now(), pageRequest);

        if (!page.hasContent()) return ResponseUtil.failure("데이터가 없습니다.", null);
        List<Notice> content = page.getContent();

        List<NoticeSimpleRetrieveDto> resultData = content.stream().map(Notice::toNoticeSimpleRetrieveDto).collect(Collectors.toList());

        return ResponseUtil.success(resultData);
    }

    @Transactional(readOnly = true)
    public ResponseDto<NoticeRetrieveDto> findById(Long noticeUid) {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        List<NoticeAttachmentDto> attachmentDtoList = attachmentRepository.findAllByNoticeUid(noticeUid)
                .stream().map(attachment -> attachment.toDto(SERVER_HTTP_URL))
                .collect(Collectors.toList());

        NoticeRetrieveDto resultData = findNotice.toNoticeRetrieveDto(attachmentDtoList);

        CompletableFuture.runAsync(() -> incrementViewCount(noticeUid));

        return ResponseUtil.success(resultData);
    }

    public void incrementViewCount(Long noticeUid) {
        viewCountCache.merge(noticeUid, 1L, Long::sum);
    }

    @Scheduled(fixedRate = 60000)
    public void syncViewCountsToDatabase() {
        for (Map.Entry<Long, Long> entry : viewCountCache.entrySet()) {
            noticeRepository.incrementViewCount(entry.getKey(), entry.getValue());
        }
        viewCountCache.clear();
    }

    public ResponseDto updateNotice(Long noticeUid, NoticeRequestDto noticeDto) {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        findNotice.update(noticeDto);
        return ResponseUtil.success(null);
    }

    public ResponseDto deleteNotice(Long noticeUid) throws Exception {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        List<Attachment> attachmentList = findNotice.getAttachmentList();
        for (Attachment attachment : attachmentList) {
            deleteFile(attachment);
        }

        attachmentRepository.deleteAll(attachmentList);
        noticeRepository.deleteById(noticeUid);

        return ResponseUtil.success(null);
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
        return ResponseUtil.success(null);
    }

    private boolean storeFile(MultipartFile file, String newFileName) {
        try {
            Path targetLocation = Paths.get(NOTICE_ATTACHMENT_URL + "/" + newFileName).toAbsolutePath().normalize();
            File dir = new File(NOTICE_ATTACHMENT_URL);
            if (!dir.exists()) {
                try {
                    dir.mkdirs();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("첨부파일 업로드에 실패했습니다. 파일 제목: " + file.getOriginalFilename(), e);
        }

    }

    public ResponseDto deleteNoticeAttachment(Long noticeUid, Long attachmentUid) throws Exception {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        Attachment findAttachment = attachmentRepository.findById(attachmentUid)
                .orElseThrow(() -> new NoSuchElementException("해당 첨부파일이 존재하지 않습니다. uid: " + noticeUid));

        if (findAttachment.getNotice().getUid() != findNotice.getUid())
            throw new NoSuchElementException("해당 게시글에 해당하는 첨부파일이 없습니다. notice uid: " + noticeUid + ", attachment uid: " + attachmentUid);

        attachmentRepository.deleteById(attachmentUid);
        deleteFile(findAttachment);

        return ResponseUtil.success(null);
    }

    @Async
    protected CompletableFuture<Void> deleteFile(Attachment findAttachment) throws Exception {
        Path path = Paths.get(NOTICE_ATTACHMENT_URL + "/" + findAttachment.getNewFileName());
        Files.delete(path);
        if (path.toFile().exists())
            throw new Exception("해당 파일 삭제에 실패했습니다. 파일이름: " + findAttachment.getNewFileName());

        return CompletableFuture.completedFuture(null);
    }
}