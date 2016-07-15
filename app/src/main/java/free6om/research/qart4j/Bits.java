package free6om.research.qart4j;

import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;

import java.util.Arrays;

/**
 * Created by free6om on 7/20/15.
 */
public class Bits {
    private byte[] bits;
    private int size;

    public Bits() {
        this.bits = new byte[1];
        this.size = 0;
    }

    public Bits(byte[] bits, int size) {
        this.bits = bits;
        this.size = size;
    }

    public void reset() {
        Arrays.fill(this.bits, (byte) 0);
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public byte[] getBits() throws QArtException {
        if(size % 8 != 0) {
            throw new QArtException("bits size error");
        }

        return bits;
    }

    public boolean get(int i) {
        return (bits[i / 8] & (1 << (7 - i & 0x07))) != 0;
    }

    public void append(boolean bit) {
        ensureCapacity(size + 1);
        if (bit) {
            bits[size / 8] |= 1 << (7 - size & 0x07);
        }
        size++;
    }

    /**
     * Appends the least-significant bits, from value, in order from most-significant to
     * least-significant. For example, appending 6 bits from 0x000001E will append the bits
     * 0, 1, 1, 1, 1, 0 in that order.
     *
     * @param value {@code int} containing bits to append
     * @param numBits bits from value to append
     */
    public void write(int value, int numBits) {
        if (numBits < 0 || numBits > 32) {
            throw new IllegalArgumentException("Num bits must be between 0 and 32");
        }
        ensureCapacity(size + numBits);
        for (int numBitsLeft = numBits; numBitsLeft > 0; numBitsLeft--) {
            append(((value >> (numBitsLeft - 1)) & 0x01) == 1);
        }
    }

    public void append(Bits other) {
        int otherSize = other.size;
        ensureCapacity(size + otherSize);
        for (int i = 0; i < otherSize; i++) {
            append(other.get(i));
        }
    }

    public void pad(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("qr: invalid pad size");
        }

        if (n <= 4) {
            this.write(0, n);
        } else {
            this.write(0, 4);
            n -= 4;
            int shift = 8 - this.size & 0x07;
            n -= shift;
            this.write(0, shift);
            int pad = n / 8;
            for (int i = 0; i < pad; i += 2) {
                this.write(0xec, 8);
                if (i+1 >= pad) {
                    break;
                }
                this.write(0x11, 8);
            }
        }
    }

    public void addCheckBytes(Version version, Level level) throws QArtException {
        int numberOfDataBytes = version.dataBytes(level);
        if (this.size < numberOfDataBytes*8) {
            pad(numberOfDataBytes*8 - this.size);
        }

        if (this.size != numberOfDataBytes*8) {
            throw new IllegalArgumentException("qr: too much data");
        }

        Version.VersionInfo versionInfo = Version.VERSION_INFOS[version.getVersion()];
        Version.VersionLevelInfo levelInfo = versionInfo.levelInfos[level.ordinal()];
        int numberOfDataBytesPerBlock = numberOfDataBytes / levelInfo.numberOfBlocks;
        int numberOfExtraBytes = numberOfDataBytes % levelInfo.numberOfBlocks;
        ReedSolomonEncoder reedSolomonEncoder = new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256);

        int dataIndex = 0;
        for(int i = 0;i < levelInfo.numberOfBlocks;i++){
            if(i == levelInfo.numberOfBlocks - numberOfExtraBytes) {
                numberOfDataBytesPerBlock++;
            }

            byte[] checkBytes = ReedSolomonUtil.generateECBytes(reedSolomonEncoder, this.bits, dataIndex, numberOfDataBytesPerBlock, levelInfo.numberOfCheckBytesPerBlock);
            dataIndex += numberOfDataBytesPerBlock;

            this.append(new Bits(checkBytes, levelInfo.numberOfCheckBytesPerBlock * 8));
        }

        if(this.size/8 != versionInfo.bytes) {
            throw new QArtException("qr: internal error");
        }

    }

    private void ensureCapacity(int size) {
        if (size > bits.length * 8) {
            byte[] newBits = makeArray(size);
            System.arraycopy(bits, 0, newBits, 0, bits.length);
            this.bits = newBits;
        }
    }

    private static byte[] makeArray(int size) {
        return new byte[(size + 7) / 8];
    }


}
