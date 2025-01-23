package dev.riss.notice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoticeControllerTest {

    private final String url = "/api/v1/notice";

    @Autowired
    private NoticeController noticeController;
    @Autowired
    private ObjectMapper mapper;
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

        Map<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("content", content);
        body.put("author", author);
        body.put("startAt", startAt);
        body.put("endAt", endAt);

        // when
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("데이터가 없습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void findAllWhenNotEmpty() throws Exception {

        // given
        setUpData();
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        String author = "이경환";

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].title").value(title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].content").value(content))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].author").value(author));
    }

    @Test
    void findByIdWhenNotEmpty() throws Exception {

        // given
        Notice notice = setUpData();

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").value(notice.getCreatedAt()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.views").value(notice.getViews()));
    }
}