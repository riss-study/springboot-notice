package dev.riss.notice.util;

import dev.riss.notice.web.dto.response.ResponseDto;

public class ResponseUtil {

    public static <T>ResponseDto<T> SUCCESS (String message, T data) {
        return new ResponseDto<>(true, message, data);
    }

    public static <T>ResponseDto<T> FAILURE (String message, T data) {
        return new ResponseDto<>(false, message, data);
    }

    public static <T>ResponseDto<T> ERROR (String message, T data) {
        return new ResponseDto<>(false, message, data);
    }
}
