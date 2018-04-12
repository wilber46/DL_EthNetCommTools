package win.wilber.commassistant.util;

import android.util.Log;

public class DataUtil {
    public static String toHexString(byte[] bytes, int length) {
        if (bytes == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toHexString(new Integer(b < (byte)0 ? b + 256 : b).intValue())).append(" ");
        }

        return result.toString();
    }

    public static boolean isAllHex(String str) {
        if (str == null) {
            return false;
        }
        char[] array = str.toCharArray();

        for (char c : array) {
            if (c != ' ' && (c < '0' || c > '9') && ((c < 'a' || c > 'f') && (c < 'A' || c > 'F'))) {
                return false;
            }
        }

        return true;
    }

    public static byte[] toByteArray(String str) {
        if (str != null) {
            int i;
            char[] NewArray = new char[65536];
            char[] array = str.toCharArray();
            int length = 0;
            for (i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            int EvenLength = length % 2 == 0 ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                i = 0;
                while (i < length) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - 48;
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = (NewArray[i] - 97) + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = (NewArray[i] - 65) + 10;
                    }
                    i++;
                }
                byte[] byteArray = new byte[(EvenLength / 2)];
                for (i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) ((data[i * 2] * 16) + data[(i * 2) + 1]);
                }
                return byteArray;
            }
        }
        return new byte[0];
    }
}
