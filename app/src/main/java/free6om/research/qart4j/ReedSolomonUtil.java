package free6om.research.qart4j;

import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;

/**
 * Created by free6om on 7/21/15.
 */
public class ReedSolomonUtil {
    public static byte[] generateECBytes(ReedSolomonEncoder encoder, byte[] dataBytes, int position, int length, int numEcBytesInBlock) {
        int numDataBytes = length;
        int[] toEncode = new int[numDataBytes + numEcBytesInBlock];
        for (int i = 0; i < numDataBytes; i++) {
            toEncode[i] = dataBytes[position + i] & 0xFF;
        }
        encoder.encode(toEncode, numEcBytesInBlock);

        byte[] ecBytes = new byte[numEcBytesInBlock];
        for (int i = 0; i < numEcBytesInBlock; i++) {
            ecBytes[i] = (byte) toEncode[numDataBytes + i];
        }
        return ecBytes;
    }
}
