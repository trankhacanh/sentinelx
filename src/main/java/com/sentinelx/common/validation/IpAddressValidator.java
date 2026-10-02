package com.sentinelx.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

    /** null hợp lệ: kết hợp với @NotBlank khi trường bắt buộc. Chuỗi rỗng thì bị từ chối. */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || IpAddresses.isValid(value);
    }
}