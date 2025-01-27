package dev.riss.notice.service;

import dev.riss.notice.domain.notice.Attachment;
import dev.riss.notice.domain.notice.AttachmentRepository;
import dev.riss.notice.domain.notice.Notice;
import dev.riss.notice.domain.notice.NoticeRepository;
import dev.riss.notice.exception.ApiException;
import dev.riss.notice.util.ResponseUtil;
import dev.riss.notice.web.dto.UidDto;
import dev.riss.notice.web.dto.request.AttachmentDeleteRequestDto;
import dev.riss.notice.web.dto.request.NoticeRequestDto;
import dev.riss.notice.web.dto.response.NoticeAttachmentDto;
import dev.riss.notice.web.dto.response.NoticeRetrieveDto;
import dev.riss.notice.web.dto.response.NoticeSimpleRetrieveDto;
import dev.riss.notice.web.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${notice.domain}")
    private String SERVER_HTTP_URL;
    @Value("${notice.attachment.directory}")
    private String NOTICE_ATTACHMENT_PATH;

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

        if (!page.hasContent()) return ResponseUtil.success(new ArrayList<>());
        List<Notice> content = page.getContent();

        List<NoticeSimpleRetrieveDto> resultData = content.stream().map(Notice::toNoticeSimpleRetrieveDto).collect(Collectors.toList());

        return ResponseUtil.success(resultData);
    }

    @Transactional(readOnly = true)
    public ResponseDto<NoticeRetrieveDto> findById(Long noticeUid) {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new ApiException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

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
                .orElseThrow(() -> new ApiException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        findNotice.update(noticeDto);
        return ResponseUtil.success(null);
    }

    public ResponseDto deleteNotice(Long noticeUid) {
        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new ApiException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        List<Attachment> attachmentList = findNotice.getAttachmentList();
        for (Attachment attachment : attachmentList) {
            deleteFile(attachment);
        }

        attachmentRepository.deleteAll(attachmentList);
        noticeRepository.deleteById(noticeUid);

        return ResponseUtil.success(null);
    }

    public ResponseDto uploadNoticeAttachment(MultipartFile[] attachments, Long noticeUid) {

        Notice findNotice = noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new ApiException("첨부파일을 업로드하려는 게시글이 존재하지 않습니다."));

        List<Attachment> attachmentList = new ArrayList<>();

        try {
            for (MultipartFile attachment : attachments) {
                String originFileName = attachment.getOriginalFilename();
                String newFileName = Attachment.toNewFileName(originFileName, findNotice.getUid());

                Attachment attachmentEntity = Attachment.builder()
                        .originFileName(originFileName)
                        .newFileName(newFileName)
                        .path(NOTICE_ATTACHMENT_PATH)
                        .notice(findNotice)
                        .build();

                attachmentList.add(attachmentEntity);

                CompletableFuture<Void> future = storeFile(attachment, newFileName);
                future.get();
            }
        } catch (Exception e) {
            deleteFiles(attachmentList);
            throw new ApiException(e.getMessage());
        }

        attachmentRepository.saveAll(attachmentList);
        return ResponseUtil.success(null);
    }

    @Async
    protected CompletableFuture<Void> storeFile(MultipartFile file, String newFileName) {
        return CompletableFuture.runAsync(() -> {
            Path targetLocation = Paths.get(NOTICE_ATTACHMENT_PATH + "/" + newFileName).toAbsolutePath().normalize();
            File dir = new File(NOTICE_ATTACHMENT_PATH);
            if (!dir.exists()) dir.mkdirs();

            try {
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ApiException("첨부파일 업로드에 실패했습니다. 파일 제목: " + file.getOriginalFilename());
            }

        }).exceptionally(ex -> {
            throw new ApiException("첨부파일 업로드에 실패했습니다. 파일 제목: " + file.getOriginalFilename());
        });
    }

    public ResponseDto deleteNoticeAttachment(Long noticeUid, AttachmentDeleteRequestDto requestDto) {
        noticeRepository.findById(noticeUid)
                .orElseThrow(() -> new ApiException("해당 게시글이 존재하지 않습니다. uid: " + noticeUid));

        ArrayList<Attachment> attachmentList = new ArrayList<>();

        for (Long attachmentUid : requestDto.getAttachmentIdList()) {
            Attachment findAttachment = attachmentRepository.findById(attachmentUid)
                    .orElseThrow(() -> new ApiException("해당 첨부파일이 존재하지 않습니다. uid: " + attachmentUid));
            if (findAttachment.getNotice().getUid() != noticeUid)
                throw new ApiException("해당 게시글에 해당하는 첨부파일이 없습니다. notice uid: " + noticeUid + ", attachment uid: " + attachmentUid);

            attachmentList.add(findAttachment);
        }

        attachmentRepository.deleteAll(attachmentList);
        deleteFiles(attachmentList);

        return ResponseUtil.success(null);
    }

    @Async
    protected void deleteFiles(List<Attachment> attachmentList) {
        for (Attachment attachment : attachmentList) {
            deleteFile(attachment);
        }
    }

    @Async
    protected CompletableFuture<Void> deleteFile(Attachment findAttachment) {
        return CompletableFuture.runAsync(() -> {
            Path path = Paths.get(NOTICE_ATTACHMENT_PATH + "/" + findAttachment.getNewFileName());
            try {
                if (!Files.exists(path)) return;
                Files.delete(path);
            } catch (IOException e) {
                throw new ApiException("해당 파일 삭제에 실패했습니다. 파일이름: " + findAttachment.getNewFileName());
            }
        });
    }
}