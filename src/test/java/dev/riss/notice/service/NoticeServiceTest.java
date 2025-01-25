package dev.riss.notice.service;

import dev.riss.notice.domain.notice.Notice;
import dev.riss.notice.domain.notice.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeServiceMock;

    @Mock
    private NoticeRepository noticeRepositoryMock;

    @Test
    void syncViewCountsToDatabase() {

        // given
        for (int i = 0; i < 3; i++)
            noticeServiceMock.incrementViewCount(1L);

        for (int i = 0; i < 2; i++)
            noticeServiceMock.incrementViewCount(2L);

        // when
        noticeServiceMock.syncViewCountsToDatabase();

        // then
        verify(noticeRepositoryMock).incrementViewCount(1L, 3L);
        verify(noticeRepositoryMock).incrementViewCount(2L, 2L);
        verifyNoMoreInteractions(noticeRepositoryMock); // 그 외의 호출은 없어야 함
    }
}