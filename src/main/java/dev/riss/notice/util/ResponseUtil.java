package dev.riss.notice.util;

import dev.riss.notice.web.dto.response.ResponseDto;

public class ResponseUtil {

    public static <T>ResponseDto<T> success (T data) {
        return new ResponseDto<>(true, "", data);
    }

    public static <T>ResponseDto<T> failure (String message, T data) {
        return new ResponseDto<>(false, message, data);
    }

    public static <T>ResponseDto<T> error (String message, T data) {
        return new ResponseDto<>(false, message, data);
    }
}
