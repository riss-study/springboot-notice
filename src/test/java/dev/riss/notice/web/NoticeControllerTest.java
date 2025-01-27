package dev.riss.notice.web;

import dev.riss.notice.domain.notice.AttachmentRepository;
import dev.riss.notice.domain.notice.Notice;
import dev.riss.notice.domain.notice.NoticeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoticeControllerTest {

    private final String url = "/api/v1/notice";

    @Autowired
    private NoticeController noticeController;
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private NoticeRepository noticeRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(noticeController).build();
    }

    @AfterEach
    void tearDown() {
        attachmentRepository.deleteAll();
        noticeRepository.deleteAll();
    }

    Notice setUpData() {
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        String author = "이경환";
        LocalDateTime startAt = LocalDateTime.parse("2025-01-24T00:00:00");
        LocalDateTime endAt = LocalDateTime.parse("2025-02-24T00:00:00");

        Notice notice = Notice
                .builder()
                .title(title)
                .content(content)
                .author(author)
                .startAt(startAt)
                .endAt(endAt)
                .views(0L)
                .build();

        return noticeRepository.save(notice);
    }

    @Test
    void createNotice() throws Exception {

        // given
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        String author = "이경환";
        String startAt = "2025-01-24 00:00:00";
        String endAt = "2025-02-24 00:00:00";

        String requestBody = String.format("""
                {
                    "title": "%s",
                    "content": "%s",
                    "author": "%s",
                    "startAt": "%s",
                    "endAt": "%s"
                }
                """, title, content, author, startAt, endAt);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findAllWhenEmpty() throws Exception {

        // given
        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.get(url+"/all")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findAllWhenNotEmpty() throws Exception {

        // given
        Notice findNotice = setUpData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.get(url+"/all")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].uid").value(findNotice.getUid()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].title").value(findNotice.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createdAt").value(findNotice.getCreatedAt().format(formatter)));
    }

    @Test
    void findByIdWhenNotEmpty() throws Exception {

        // given
        Notice notice = setUpData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.get(url + "/" + notice.getUid())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.uid").value(notice.getUid()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(notice.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(notice.getContent()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.author").value(notice.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").value(notice.getCreatedAt().format(formatter)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.views").value(notice.getViews()));
    }

    @Test
    void updateNotice () throws Exception {

        // given
        Notice notice = setUpData();
        String title = "바뀐 공지사항 제목";
        String content = "바뀐 공지사항 내용";
        String author = "바뀐 이경환";
        String startAt = "2025-01-24 01:00:00";
        String endAt = "2025-02-24 01:00:00";

        String requestBody = String.format("""
                {
                    "title": "%s",
                    "content": "%s",
                    "author": "%s",
                    "startAt": "%s",
                    "endAt": "%s"
                }
                """, title, content, author, startAt, endAt);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.put(url + "/" + notice.getUid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty());

        Notice findNotice = noticeRepository.findById(notice.getUid())
                .get();

        assertEquals(title, findNotice.getTitle());
        assertEquals(content, findNotice.getContent());
        assertEquals(author, findNotice.getAuthor());
        assertEquals(LocalDateTime.parse(startAt, formatter), findNotice.getStartAt());
        assertEquals(LocalDateTime.parse(endAt, formatter), findNotice.getEndAt());
    }

    @Test
    void deleteNotice () throws Exception {

        // given
        Notice notice = setUpData();

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.delete(url + "/" + notice.getUid())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty());

        assertThrows(NoSuchElementException.class, () -> noticeRepository.findById(notice.getUid()).get());

    }
}