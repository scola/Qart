package free6om.research.qart4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by free6om on 7/20/15.
 */
public class Plan {
    private static final Logger LOGGER = LoggerFactory.getLogger(Plan.class);

    private Version version;
    private Level level;
    private Mask mask;

    private int numberOfDataBytes;
    private int numberOfCheckBytes;
    private int numberOfBlocks;

    private Pixel[][] pixels;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Mask getMask() {
        return mask;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
    }

    public int getNumberOfDataBytes() {
        return numberOfDataBytes;
    }

    public void setNumberOfDataBytes(int numberOfDataBytes) {
        this.numberOfDataBytes = numberOfDataBytes;
    }

    public int getNumberOfCheckBytes() {
        return numberOfCheckBytes;
    }

    public void setNumberOfCheckBytes(int numberOfCheckBytes) {
        this.numberOfCheckBytes = numberOfCheckBytes;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public Pixel[][] getPixels() {
        return pixels;
    }

    public void setPixels(Pixel[][] pixels) {
        this.pixels = pixels;
    }

    public static Plan newPlan(Version version, Level level, Mask mask) throws QArtException {
        Plan plan = versionPlan(version);
        formatPlan(plan, level, mask);

        levelPlan(plan, version, level);
        maskPlan(plan, mask);

        return plan;
    }

    private static void maskPlan(Plan plan, Mask mask) {
        plan.setMask(mask);
        for(int y = 0;y < plan.pixels.length;y++) {
            for(int x = 0;x < plan.pixels[y].length;x++) {
                Pixel pixel = plan.pixels[y][x];
                Pixel.PixelRole role = pixel.getPixelRole();
                if((role == Pixel.PixelRole.DATA || role == Pixel.PixelRole.CHECK || role == Pixel.PixelRole.EXTRA) &&
                        plan.mask.shouldInvert(y, x)) {
                    pixel.setInvert(!pixel.shouldInvert());
                    pixel.xorPixel(Pixel.BLACK.getPixel());
                }
            }
        }
    }

    private static void levelPlan(Plan plan, Version version, Level level) throws QArtException {
        plan.setLevel(level);

        int numberOfBlocks = Version.VERSION_INFOS[version.getVersion()].levelInfos[level.ordinal()].numberOfBlocks;
        int numberOfCheckBytes = Version.VERSION_INFOS[version.getVersion()].levelInfos[level.ordinal()].numberOfCheckBytesPerBlock;
        int numberOfDataBytes = (Version.VERSION_INFOS[version.getVersion()].bytes - numberOfCheckBytes*numberOfBlocks) / numberOfBlocks;
        int numberOfExtraBytes = (Version.VERSION_INFOS[version.getVersion()].bytes - numberOfCheckBytes*numberOfBlocks) % numberOfBlocks;
        int dataBits = (numberOfDataBytes*numberOfBlocks + numberOfExtraBytes) * 8;
        int checkBits = numberOfCheckBytes * numberOfBlocks * 8;

        plan.setNumberOfDataBytes(Version.VERSION_INFOS[version.getVersion()].bytes - numberOfCheckBytes * numberOfBlocks);
        plan.setNumberOfCheckBytes(numberOfCheckBytes * numberOfBlocks);
        plan.setNumberOfBlocks(numberOfBlocks);

        // Make data + checksum pixels.
        Pixel[] data = new Pixel[dataBits];
        for (int i = 0;i < dataBits; i++) {
            Pixel pixel = new Pixel(Pixel.PixelRole.DATA);
            pixel.setOffset(i);
            data[i] = pixel;
        }
        Pixel[] check = new Pixel[checkBits];
        for (int i = 0;i < checkBits; i++) {
            Pixel pixel = new Pixel(Pixel.PixelRole.CHECK);
            pixel.setOffset(i + dataBits);
            check[i] = pixel;
        }

        // Split into blocks.
        Pixel[][] dataList = new Pixel[numberOfBlocks][];
        Pixel[][] checkList = new Pixel[numberOfBlocks][];
        int dataIndex = 0, checkIndex = 0;
        for (int i = 0; i < numberOfBlocks; i++) {
            // The last few blocks have an extra data byte (8 pixels).
            int dataBytes = numberOfDataBytes;
            if (i >= numberOfBlocks - numberOfExtraBytes) {
                dataBytes++;
            }
            Pixel[] dataBLock = new Pixel[dataBytes * 8];
            System.arraycopy(data, dataIndex, dataBLock, 0, dataBLock.length);
            dataList[i] = dataBLock;
            Pixel[] checkBlock = new Pixel[numberOfCheckBytes * 8];
            System.arraycopy(check, checkIndex, checkBlock, 0, checkBlock.length);
            checkList[i] = checkBlock;

            dataIndex += dataBLock.length;
            checkIndex += checkBlock.length;
        }

        if(dataIndex != dataBits || checkIndex != checkBits) {
            throw new QArtException("build data/check block error");
        }

        // Build up bit sequence, taking first byte of each block,
        // then second byte, and so on.  Then checksums.
        Pixel[] bits = new Pixel[dataBits + checkBits];
        int bitIndex = 0;

        for(int i = 0;i < numberOfDataBytes + 1;i++) {
            for(int j = 0;j < dataList.length; j++) {
                if(i * 8 < dataList[j].length) {
                    System.arraycopy(dataList[j], i*8, bits, bitIndex, 8);
                    bitIndex += 8;
                }
            }
        }

        for(int i = 0;i < numberOfCheckBytes; i++) {
            for(int j = 0;j < checkList.length;j++) {
                if(i*8 < checkList[j].length) {
                    System.arraycopy(checkList[j], i * 8, bits, bitIndex, 8);
                    bitIndex += 8;
                }
            }
        }

        if(bitIndex != bits.length) {
            throw new QArtException("copy to bit error");
        }

        // Sweep up pair of columns,
        // then down, assigning to right then left pixel.
        // Repeat.
        // See Figure 2 of http://www.pclviewer.com/rs2/qrtopology.htm
        int size = plan.pixels.length;
        Pixel[] rem = new Pixel[7];
        for(int i = 0;i < rem.length;i++) {
            rem[i] = new Pixel(Pixel.PixelRole.EXTRA);
        }

        Pixel[] src = new Pixel[bits.length + rem.length];
        System.arraycopy(bits, 0, src, 0, bits.length);
        System.arraycopy(rem, 0, src, bits.length, rem.length);
        int srcIndex = 0;

        for (int x = size; x > 0;) {
            for (int y = size - 1; y >= 0; y--) {
                if (plan.pixels[y][x-1].getPixelRole().ordinal() == 0) {
                    plan.pixels[y][x-1] = src[srcIndex++];
                }
                if (plan.pixels[y][x-2].getPixelRole().ordinal() == 0) {
                    plan.pixels[y][x-2] = src[srcIndex++];
                }
            }
            x -= 2;
            if (x == 7) { // vertical timing strip
                x--;
            }
            for (int y = 0; y < size; y++) {
                if (plan.pixels[y][x - 1].getPixelRole().ordinal() == 0) {
                    plan.pixels[y][x - 1] = src[srcIndex++];
                }
                if (plan.pixels[y][x - 2].getPixelRole().ordinal() == 0) {
                    plan.pixels[y][x - 2] = src[srcIndex++];
                }
            }
            x -= 2;
        }
    }

    private static void formatPlan(Plan plan, Level level, Mask mask) {
        // Format pixels.
        int formatBit = (level.ordinal()^1) << 13; // level: L=01, M=00, Q=11, H=10
        formatBit |= mask.getMask() << 10;   // mask
        int formatPoly = 0x537;
        int rem = formatBit;
        for (int i = 14; i >= 10; i--) {
            if ((rem&(1<<i)) != 0) {
                rem ^= (formatPoly << (i-10));
            }
        }
        formatBit |= rem;
        int invert = 0x5412;
        int size = plan.getPixels().length;
        for (int i = 0; i < 15; i++) {
            Pixel pixel = new Pixel(Pixel.PixelRole.FORMAT);
            pixel.setOffset(i);
            if (((formatBit>>i)&1) == 1) {
                pixel.orPixel(Pixel.BLACK.getPixel());
            }
            if (((invert>>i)&1) == 1) {
                pixel.setInvert(!pixel.shouldInvert());
                pixel.xorPixel(Pixel.BLACK.getPixel());
            }
            // top left
            if(i < 6) {
                plan.pixels[i][8] = pixel;
            } else if(i < 8) {
                plan.pixels[i + 1][8] = pixel;
            } else if(i < 9) {
                plan.pixels[8][7] = pixel;
            } else {
                plan.pixels[8][14-i] = pixel;
            }
            // bottom right
            if(i < 8) {
                plan.pixels[8][size - 1 - i]=pixel;
            } else {
                plan.pixels[size - 1 - (14-i)][8] = pixel;
            }
        }
    }

    public static Plan versionPlan(Version version) throws VersionException {
        Plan plan = new Plan();
        plan.setVersion(version);
        if(version.getVersion() < Version.MIN_VERSION || version.getVersion() > Version.MAX_VERSION) {
            throw new VersionException("wrong qr version: " + version.getVersion());
        }

        int size = 17 + 4 * version.getVersion();
        Pixel[][] pixels = new Pixel[size][size];
        plan.setPixels(pixels);
        for(int y = 0;y < size;y++) {
            Arrays.fill(pixels[y], new Pixel(0));
        }

        int timingPosition = 6;
        for(int i = 0;i < size;i++) {
            Pixel pixel = new Pixel(Pixel.PixelRole.TIMING);
            if((i&1) == 0) {
                pixel.orPixel(Pixel.BLACK.getPixel());
            }

            pixels[i][timingPosition] = pixel;
            pixels[timingPosition][i] = pixel;
        }

        //position box
        setPositionBox(pixels, 0, 0);
        setPositionBox(pixels, size - 7, 0);
        setPositionBox(pixels, 0, size - 7);

        //Alignment box
        Version.VersionInfo versionInfo = Version.VERSION_INFOS[version.getVersion()];
        for(int x = 4;x + 5 < size;) {
            for(int y = 4;y + 5 < size;) {
                // don't overwrite timing markers
                if ((x < 7 && y < 7) || (x < 7 && y+5 >= size-7) || (x+5 >= size-7 && y < 7)) {
                } else {
                    setAlignBox(pixels, x, y);
                }
                if (y == 4) {
                    y = versionInfo.apos;
                } else {
                    y += versionInfo.stride;
                }
            }

            if (x == 4) {
                x = versionInfo.apos;
            } else {
                x += versionInfo.stride;
            }
        }

        //version pattern
        int pattern = versionInfo.pattern;
        if(pattern != 0) {
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 3; y++) {
                    Pixel pixel = new Pixel(Pixel.PixelRole.VERSION_PATTERN);
                    if ((pattern&1) != 0) {
                        pixel.orPixel(Pixel.BLACK.getPixel());
                    }

                    pixels[size-11+y][x] = pixel;
                    pixels[x][size-11+y] = pixel;
                    pattern >>= 1;
                }
            }
        }

        Pixel pixel = new Pixel(Pixel.PixelRole.UNUSED);
        pixel.orPixel(Pixel.BLACK.getPixel());
        pixels[size - 8][8] = pixel;

        return plan;
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    public static QRCode encode(Plan plan, Encoding... encodings) throws QArtException {
        Bits bits = new Bits();
        for(Encoding encoding : encodings) {
            String error = encoding.validate();
            if(error != null) {
                throw new QArtException("encoding check error: " + error);
            }
            encoding.encode(bits, plan.version);
        }
        if(bits.getSize() > plan.getNumberOfDataBytes()*8) {
            throw new QArtException("cannot encode " + bits.getSize() + " bits into " + (plan.getNumberOfDataBytes()*8) + "-bit code");
        }
        bits.addCheckBytes(plan.version, plan.level);
        byte[] bytes = bits.getBits();

        Pixel[][] pixels = plan.pixels;

        return new QRCode(bytes, pixels);


    }

    private static void setAlignBox(Pixel[][] pixels, int x, int y) {
        // box
        Pixel pixelWhite = new Pixel(Pixel.PixelRole.ALIGNMENT);
        Pixel pixelBlack = new Pixel(Pixel.PixelRole.ALIGNMENT);
        pixelBlack.setPixel(Pixel.BLACK.getPixel());

        for (int dy = 0; dy < 5; dy++) {
            for (int dx = 0; dx < 5; dx++) {
                if (dx == 0 || dx == 4 || dy == 0 || dy == 4 || dx == 2 && dy == 2) {
                    pixels[y+dy][x+dx] = pixelBlack;
                } else {
                    pixels[y + dy][x + dx] = pixelWhite;
                }
            }
        }
    }

    private static void setPositionBox(Pixel[][] pixels, int x, int y) {
        Pixel pixelWhite = new Pixel(Pixel.PixelRole.POSITION);
        Pixel pixelBlack = new Pixel(Pixel.PixelRole.POSITION);
        pixelBlack.setPixel(Pixel.BLACK.getPixel());

        //box
        for (int dy = 0; dy < 7; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                if (dx == 0 || dx == 6 || dy == 0 || dy == 6 || 2 <= dx && dx <= 4 && 2 <= dy && dy <= 4) {
                    pixels[y+dy][x+dx] = pixelBlack;
                } else {
                    pixels[y + dy][x + dx] = pixelWhite;
                }
            }
        }

        // white border
        for (int dy = -1; dy < 8; dy++) {
            if (0 <= y+dy && y+dy < pixels.length) {
                if (x > 0) {
                    pixels[y+dy][x-1] = pixelWhite;
                }
                if (x+7 < pixels.length) {
                    pixels[y+dy][x+7] = pixelWhite;
                }
            }
        }

        for (int dx = -1; dx < 8; dx++) {
            if (0 <= x+dx && x+dx < pixels.length) {
                if (y > 0) {
                    pixels[y-1][x+dx] = pixelWhite;
                }
                if (y+7 < pixels.length) {
                    pixels[y+7][x+dx] = pixelWhite;
                }
            }
        }
    }

}
