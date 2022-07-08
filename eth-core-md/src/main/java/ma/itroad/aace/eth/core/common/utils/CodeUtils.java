package ma.itroad.aace.eth.core.common.utils;

import java.util.Random;

public class CodeUtils {
    private static final String PREFIX = "D";
    private static final String CERTIFICATE_PREFIX = "C-";
    private static final String REFERENCE_PREFIX = "ETH-";

    public static String generateOTP() {
        String numbers = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int rand = new Random().nextInt(numbers.length());
            sb.append(numbers, rand, rand+1);
        }
        return sb.toString();
    }

    public static String generateCustomOTP() {
        String otp = generateOTP();
        return PREFIX.concat(otp);
    }

    public static String generateReference() {
        String otp = generateOTP();
        return PREFIX.concat(otp);
    }

    public static String generateCertificateReference() {
        String otp = generateOTP();
        return CERTIFICATE_PREFIX
                .concat(otp)
                .concat("-")
                .concat(String.valueOf(System.currentTimeMillis()));
    }
}
