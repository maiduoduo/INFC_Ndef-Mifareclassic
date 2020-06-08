package com.gyg.nfc;

/**
 * NFC M1卡片工具
 */

public class NFCUtil {

    public static String byte2HexString(byte[] bytes) {
        String ret = "";
        if (bytes != null) {
            for (Byte b : bytes) {
                ret += String.format("%02X", b.intValue() & 0xFF);
            }
        }
        return ret;
    }

    public static byte[] string2byte(String inputStr) {
        byte[] result = new byte[inputStr.length() / 2];
        for (int i = 0; i < inputStr.length() / 2; ++i)
            result[i] = (byte) (Integer.parseInt(inputStr.substring(i * 2, i * 2 + 2), 16) & 0xff);
        return result;
    }
}
