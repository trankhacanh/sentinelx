package com.sentinelx.common.exception;

/** Yêu cầu hợp lệ về cú pháp nhưng sai về ngữ nghĩa (ví dụ from > to). Map sang HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}