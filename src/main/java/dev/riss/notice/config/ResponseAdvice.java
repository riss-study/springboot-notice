package dev.riss.notice.config;

import dev.riss.notice.exception.ApiException;
import dev.riss.notice.util.ResponseUtil;
import dev.riss.notice.web.dto.response.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Component
@Slf4j
public class ResponseAdvice {

    @ExceptionHandler({ApiException.class})
    public ResponseDto exception(ApiException e) {
        log.error(e.getMessage());
        return ResponseUtil.failure(e.getMessage(), null);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseDto exception(IllegalArgumentException e) {
        log.error(e.getMessage());
        return ResponseUtil.error(e.getMessage(), null);
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseDto exception(NoSuchElementException e) {
        log.error(e.getMessage());
        return ResponseUtil.error(e.getMessage(), null);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseDto exception(RuntimeException e) {
        log.error(e.getMessage());
        return ResponseUtil.error(e.getMessage(), null);
    }

    @ExceptionHandler({Exception.class})
    public ResponseDto exception(Exception e) {
        log.error(e.getMessage());
        return ResponseUtil.error(e.getMessage(), null);
    }
}
