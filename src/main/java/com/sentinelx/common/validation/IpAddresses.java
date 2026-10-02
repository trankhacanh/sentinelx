package com.sentinelx.common.validation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Kiểm tra và chuẩn hóa địa chỉ IP mà KHÔNG BAO GIỜ thực hiện DNS lookup.
 * InetAddress.getByName("abc") sẽ đi phân giải tên miền, nên ta chỉ gọi nó khi chuỗi
 * chắc chắn là IPv6 literal (chứa ':' và chỉ gồm ký tự hex, ':' và '.').
 */
public final class IpAddresses {

    private static final String OCTET = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
    private static final Pattern IPV4 = Pattern.compile("^(" + OCTET + "\\.){3}" + OCTET + "$");
    private static final Pattern IPV6_CHARS = Pattern.compile("^[0-9a-fA-F:.]+$");
    private static final int MAX_LENGTH = 45;

    private IpAddresses() {
    }

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        String s = value.trim();
        if (IPV4.matcher(s).matches()) {
            return true;
        }
        return parseIpv6Literal(s) != null;
    }

    /** Trả về dạng chuẩn để cùng một IP luôn cho cùng một chuỗi. Chỉ gọi sau khi isValid() = true. */
    public static String normalize(String value) {
        String s = value.trim();
        if (IPV4.matcher(s).matches()) {
            return s;
        }
        InetAddress address = parseIpv6Literal(s);
        if (address == null) {
            throw new IllegalArgumentException("Not a valid IP address");
        }
        return address.getHostAddress();
    }

    private static InetAddress parseIpv6Literal(String s) {
        if (s.length() > MAX_LENGTH || s.indexOf(':') < 0 || !IPV6_CHARS.matcher(s).matches()) {
            return null;
        }
        try {
            return InetAddress.getByName(s);
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}