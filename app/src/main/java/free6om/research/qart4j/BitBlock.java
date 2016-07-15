package free6om.research.qart4j;

import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;

import java.util.Arrays;

/**
 * Created by free6om on 7/21/15.
 */
public class BitBlock {
    private int numberOfDataBytes;
    private int numberOfCheckBytes;
    private byte[] blockBytes;
    private byte[][] maskMatrix;
    private int maskIndex;
    private ReedSolomonEncoder encoder;
    private byte[] primaryDataBytes;
    private int primaryDataIndex;
    private byte[] primaryCheckBytes;
    private int primaryCheckIndex;

    public BitBlock(int numberOfDataBytes, int numberOfCheckBytes, ReedSolomonEncoder encoder, byte[] primaryDataBytes, int primaryDataIndex, byte[] primaryCheckBytes, int primaryCheckIndex) throws QArtException {
        this.numberOfDataBytes = numberOfDataBytes;
        this.numberOfCheckBytes = numberOfCheckBytes;
        this.encoder = encoder;
        this.blockBytes = new byte[numberOfDataBytes + numberOfCheckBytes];
        this.primaryDataBytes = primaryDataBytes;
        this.primaryDataIndex = primaryDataIndex;
        this.primaryCheckBytes = primaryCheckBytes;
        this.primaryCheckIndex = primaryCheckIndex;

        System.arraycopy(primaryDataBytes, primaryDataIndex, blockBytes, 0, numberOfDataBytes);
        byte[] checkBytes = ReedSolomonUtil.generateECBytes(encoder, blockBytes, 0, numberOfDataBytes, numberOfCheckBytes);
        System.arraycopy(checkBytes, 0, blockBytes, numberOfDataBytes, numberOfCheckBytes);

        byte[] expectCheckBytes = new byte[numberOfCheckBytes];
        System.arraycopy(primaryCheckBytes, primaryCheckIndex, expectCheckBytes, 0, numberOfCheckBytes);
        if(!Arrays.equals(expectCheckBytes, checkBytes)) {
            throw new QArtException("check data not match");
        }

        this.maskMatrix = new byte[numberOfDataBytes*8][numberOfDataBytes + numberOfCheckBytes];
        this.maskIndex = this.maskMatrix.length;
        for(int i = 0;i < numberOfDataBytes*8;i++) {
            for(int j = 0;j < numberOfDataBytes + numberOfCheckBytes;j++) {
                maskMatrix[i][j] = 0;
            }

            maskMatrix[i][i/8] = (byte) (1 << (7 - i%8));
            checkBytes = ReedSolomonUtil.generateECBytes(encoder, maskMatrix[i], 0, numberOfDataBytes, numberOfCheckBytes);
            System.arraycopy(checkBytes, 0, maskMatrix[i], numberOfDataBytes, numberOfCheckBytes);
        }
    }

    public byte[] getBlockBytes() {
        return blockBytes;
    }

    public void check() throws QArtException {
        byte[] checkBytes = ReedSolomonUtil.generateECBytes(encoder, blockBytes, 0, numberOfDataBytes, numberOfCheckBytes);
        byte[] expectCheckBytes = new byte[numberOfCheckBytes];
        System.arraycopy(blockBytes, numberOfDataBytes, expectCheckBytes, 0, numberOfCheckBytes);

        if(!Arrays.equals(expectCheckBytes, checkBytes)) {
            throw new QArtException("ecc mismatch");
        }
    }

    public void reset(int index, byte value) throws QArtException {
        if (((blockBytes[index/8]>>(7-index&7))&1) == (value&1)) {
            // already has desired bit
            return;
        }

        for(int i = this.maskIndex;i < this.maskMatrix.length;i++) {
            byte[] row = this.maskMatrix[i];
            if((row[index/8]&(1<<(7-index&7))) != 0) {
                for(int j = 0;j < row.length;j++) {
                    blockBytes[j] ^= (value&1);
                }

                return;
            }
        }

        throw new QArtException("reset of unset bit");
    }

    public boolean canSet(int index, byte value) throws QArtException {
        boolean found = false;
        for(int j = 0;j < maskIndex;j++) {
            if((maskMatrix[j][index/8]&(1<<(7-index&7))) == 0) {
                continue;
            }

            if(!found) {
                found = true;
                if(j != 0) {
                    exchangeRow(maskMatrix, 0, j);
                }
                continue;
            }

            for(int k = 0;k < maskMatrix[j].length;k++) {
                maskMatrix[j][k] ^= maskMatrix[0][k];
            }
        }

        if(!found) {
            return false;
        }

        // Subtract from saved-away rows too.
        byte[] target = maskMatrix[0];
        for(int i = maskIndex;i < maskMatrix.length;i++) {
            byte[] row = maskMatrix[i];
            if((row[index/8]&(1<<(7-index&7))) == 0) {
                continue;
            }
            for(int k = 0;k < row.length;k++) {
                row[k] ^= target[k];
            }
        }

        // Found a row with bit #bi == 1 and cut that bit from all the others.
        // Apply to data and remove from m.
        if(((blockBytes[index/8]>>(7-index&7))&1) != (value&1)) {
            for(int j = 0;j < target.length;j++) {
                byte v = target[j];
                blockBytes[j] ^= v;
            }
        }

        this.check();
        exchangeRow(maskMatrix, 0, maskIndex - 1);
        maskIndex--;

        for(int i = 0;i < maskIndex;i++) {
            byte[] row = maskMatrix[i];
            if((row[index/8]&(1<<(7-index&7))) != 0) {
                throw new QArtException("did not reduce");
            }
        }

        return true;

    }

    public void copyOut() throws QArtException {
        check();

        System.arraycopy(blockBytes, 0, primaryDataBytes, primaryDataIndex, numberOfDataBytes);
        System.arraycopy(blockBytes, numberOfDataBytes, primaryCheckBytes, primaryCheckIndex, numberOfCheckBytes);
    }

    private void exchangeRow(byte[][] matrix, int i, int j) {
        byte[] tmp = new byte[matrix[i].length];
        System.arraycopy(maskMatrix[i], 0, tmp, 0, tmp.length);
        System.arraycopy(maskMatrix[j], 0, maskMatrix[i], 0, maskMatrix[i].length);
        System.arraycopy(tmp, 0, maskMatrix[j], 0, tmp.length);
    }
}
