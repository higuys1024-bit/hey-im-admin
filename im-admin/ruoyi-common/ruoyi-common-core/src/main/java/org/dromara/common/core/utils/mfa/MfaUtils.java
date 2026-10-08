package org.dromara.common.core.utils.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * MFA (基于 RFC 6238 TOTP 标准) 身份验证工具类
 * 兼容 Google Authenticator、Microsoft Authenticator 等主流动态口令 APP
 *
 * @author Lion Li
 */
@Slf4j
public class MfaUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final long TIME_STEP_SECONDS = 30L;
    private static final int DEFAULT_WINDOW = 1; // 允许前后 1 个时间片（共 90 秒窗口容错）

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成随机 16 字节 Base32 密钥 (共 32 字符，兼容谷歌验证器)
     */
    public static String generateSecret() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * 校验 6 位动态口令
     *
     * @param secret Base32 密钥
     * @param code   6 位用户输入验证码
     * @return 是否通过
     */
    public static boolean verifyCode(String secret, String code) {
        return verifyCode(secret, code, DEFAULT_WINDOW);
    }

    /**
     * 校验 6 位动态口令（可自定义时间窗口）
     *
     * @param secret Base32 密钥
     * @param code   6 位用户输入验证码
     * @param window 容错时间片（例如 1 表示前后各 30 秒容错）
     * @return 是否通过
     */
    public static boolean verifyCode(String secret, String code, int window) {
        if (StringUtils.isBlank(secret) || StringUtils.isBlank(code)) {
            return false;
        }
        code = code.trim();
        if (code.length() != 6 || !StringUtils.isNumeric(code)) {
            return false;
        }

        byte[] keyBytes;
        try {
            keyBytes = decodeBase32(secret);
        } catch (Exception e) {
            log.error("MFA secret Base32 解码失败: {}", e.getMessage());
            return false;
        }

        long currentSeconds = System.currentTimeMillis() / 1000L;
        long currentStep = currentSeconds / TIME_STEP_SECONDS;

        for (int i = -window; i <= window; i++) {
            long step = currentStep + i;
            String expectedCode = generateTotp(keyBytes, step);
            if (code.equals(expectedCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据时间片计算 6 位动态验证码
     */
    public static String generateTotp(byte[] key, long step) {
        try {
            byte[] data = ByteBuffer.allocate(8).putLong(step).array();
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

            int code = binary % 1_000_000;
            return String.format("%06d", code);
        } catch (Exception e) {
            log.error("生成 TOTP 验证码异常: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 生成 Google Authenticator 二维码对应的标准 URI
     *
     * @param account 用户名/账号
     * @param secret  MFA 密钥
     * @param issuer  服务/公司名称
     * @return otpauth://totp/... URI
     */
    public static String generateOtpAuthUri(String account, String secret, String issuer) {
        try {
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");
            String encodedAccount = URLEncoder.encode(account, StandardCharsets.UTF_8).replace("+", "%20");
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer, encodedAccount, secret, encodedIssuer);
        } catch (Exception e) {
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, account, secret, issuer);
        }
    }

    /**
     * Base32 编码 (RFC 4648 无填充)
     */
    public static String encodeBase32(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        while (next < data.length) {
            buffer <<= 8;
            buffer |= (data[next++] & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                result.append(BASE32_CHARS.charAt((buffer >> (bitsLeft - 5)) & 0x1F));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            result.append(BASE32_CHARS.charAt(buffer & 0x1F));
        }
        return result.toString();
    }

    /**
     * Base32 解码 (RFC 4648)
     */
    public static byte[] decodeBase32(String base32) {
        if (StringUtils.isBlank(base32)) {
            return new byte[0];
        }
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        ByteBuffer bytes = ByteBuffer.allocate((clean.length() * 5) / 8);
        int buffer = 0;
        int bitsLeft = 0;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) {
                continue;
            }
            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes.put((byte) ((buffer >> (bitsLeft - 8)) & 0xFF));
                bitsLeft -= 8;
            }
        }
        return Arrays.copyOf(bytes.array(), bytes.position());
    }
}
