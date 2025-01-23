package dev.riss.notice.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private final boolean success;
    private final String message;
    private final T data;
}