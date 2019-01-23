package com.manridy.iband.common;
/**
 * 字符串转字节工具类
 */
public class HexUtil {

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1])) & 0xff);
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] b) {
        if (b.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < b.length; i++) {
            int value = b[i] & 0xFF;
            String hv = Integer.toHexString(value);
            if (hv.length() < 2) {
                sb.append(0);
            }

            sb.append(hv);
        }
        return sb.toString();
    }
    private static final String number = "0123456789ABCDEF";

    /** make a hex string from the number (0-65535) */
    public static String convertToString16(int dex) throws OutofRangeException{

        if((dex < 0) || (dex > 65535)){
            throw new OutofRangeException("number is large than 65535,must be in (0-65535)");
        }

        int a = dex / 256;
        int b = dex % 256;

        String builder = String.valueOf(number.charAt((a >> 4) & 0x0F)) +
                number.charAt(a & 0xF) +
                number.charAt((b >> 4) & 0x0F) +
                number.charAt(b & 0x0F);
        return builder;
    }
}
