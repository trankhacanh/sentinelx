package com.sentinelx.common.exception;

/** Vi phạm ràng buộc nghiệp vụ như trùng username/email. Map sang HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}