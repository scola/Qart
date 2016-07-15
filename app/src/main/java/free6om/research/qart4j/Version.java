package free6om.research.qart4j;

/**
 * Created by free6om on 7/20/15.
 */
public class Version {
    public static final int MIN_VERSION = 1;
    public static final int MAX_VERSION = 40;

    public static final VersionInfo[] VERSION_INFOS = new VersionInfo[]{
        null,
        new VersionInfo(100, 100, 26, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(1, 7), new VersionLevelInfo(1, 10), new VersionLevelInfo(1, 13), new VersionLevelInfo(1, 17)}),          // 1
        new VersionInfo(16, 100, 44, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(1, 10), new VersionLevelInfo(1, 16), new VersionLevelInfo(1, 22), new VersionLevelInfo(1, 28)}),          // 2
        new VersionInfo(20, 100, 70, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(1, 15), new VersionLevelInfo(1, 26), new VersionLevelInfo(2, 18), new VersionLevelInfo(2, 22)}),          // 3
        new VersionInfo(24, 100, 100, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(1, 20), new VersionLevelInfo(2, 18), new VersionLevelInfo(2, 26), new VersionLevelInfo(4, 16)}),         // 4
        new VersionInfo(28, 100, 134, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(1, 26), new VersionLevelInfo(2, 24), new VersionLevelInfo(4, 18), new VersionLevelInfo(4, 22)}),         // 5
        new VersionInfo(32, 100, 172, 0x0, new VersionLevelInfo[]{new VersionLevelInfo(2, 18), new VersionLevelInfo(4, 16), new VersionLevelInfo(4, 24), new VersionLevelInfo(4, 28)}),         // 6
        new VersionInfo(20, 16, 196, 0x7c94, new VersionLevelInfo[]{new VersionLevelInfo(2, 20), new VersionLevelInfo(4, 18), new VersionLevelInfo(6, 18), new VersionLevelInfo(5, 26)}),       // 7
        new VersionInfo(22, 18, 242, 0x85bc, new VersionLevelInfo[]{new VersionLevelInfo(2, 24), new VersionLevelInfo(4, 22), new VersionLevelInfo(6, 22), new VersionLevelInfo(6, 26)}),       // 8
        new VersionInfo(24, 20, 292, 0x9a99, new VersionLevelInfo[]{new VersionLevelInfo(2, 30), new VersionLevelInfo(5, 22), new VersionLevelInfo(8, 20), new VersionLevelInfo(8, 24)}),       // 9
        new VersionInfo(26, 22, 346, 0xa4d3, new VersionLevelInfo[]{new VersionLevelInfo(4, 18), new VersionLevelInfo(5, 26), new VersionLevelInfo(8, 24), new VersionLevelInfo(8, 28)}),       // 10
        new VersionInfo(28, 24, 404, 0xbbf6, new VersionLevelInfo[]{new VersionLevelInfo(4, 20), new VersionLevelInfo(5, 30), new VersionLevelInfo(8, 28), new VersionLevelInfo(11, 24)}),      // 11
        new VersionInfo(30, 26, 466, 0xc762, new VersionLevelInfo[]{new VersionLevelInfo(4, 24), new VersionLevelInfo(8, 22), new VersionLevelInfo(10, 26), new VersionLevelInfo(11, 28)}),     // 12
        new VersionInfo(32, 28, 532, 0xd847, new VersionLevelInfo[]{new VersionLevelInfo(4, 26), new VersionLevelInfo(9, 22), new VersionLevelInfo(12, 24), new VersionLevelInfo(16, 22)}),     // 13
        new VersionInfo(24, 20, 581, 0xe60d, new VersionLevelInfo[]{new VersionLevelInfo(4, 30), new VersionLevelInfo(9, 24), new VersionLevelInfo(16, 20), new VersionLevelInfo(16, 24)}),     // 14
        new VersionInfo(24, 22, 655, 0xf928, new VersionLevelInfo[]{new VersionLevelInfo(6, 22), new VersionLevelInfo(10, 24), new VersionLevelInfo(12, 30), new VersionLevelInfo(18, 24)}),    // 15
        new VersionInfo(24, 24, 733, 0x10b78, new VersionLevelInfo[]{new VersionLevelInfo(6, 24), new VersionLevelInfo(10, 28), new VersionLevelInfo(17, 24), new VersionLevelInfo(16, 30)}),   // 16
        new VersionInfo(28, 24, 815, 0x1145d, new VersionLevelInfo[]{new VersionLevelInfo(6, 28), new VersionLevelInfo(11, 28), new VersionLevelInfo(16, 28), new VersionLevelInfo(19, 28)}),   // 17
        new VersionInfo(28, 26, 901, 0x12a17, new VersionLevelInfo[]{new VersionLevelInfo(6, 30), new VersionLevelInfo(13, 26), new VersionLevelInfo(18, 28), new VersionLevelInfo(21, 28)}),   // 18
        new VersionInfo(28, 28, 991, 0x13532, new VersionLevelInfo[]{new VersionLevelInfo(7, 28), new VersionLevelInfo(14, 26), new VersionLevelInfo(21, 26), new VersionLevelInfo(25, 26)}),   // 19
        new VersionInfo(32, 28, 1085, 0x149a6, new VersionLevelInfo[]{new VersionLevelInfo(8, 28), new VersionLevelInfo(16, 26), new VersionLevelInfo(20, 30), new VersionLevelInfo(25, 28)}),  // 20
        new VersionInfo(26, 22, 1156, 0x15683, new VersionLevelInfo[]{new VersionLevelInfo(8, 28), new VersionLevelInfo(17, 26), new VersionLevelInfo(23, 28), new VersionLevelInfo(25, 30)}),  // 21
        new VersionInfo(24, 24, 1258, 0x168c9, new VersionLevelInfo[]{new VersionLevelInfo(9, 28), new VersionLevelInfo(17, 28), new VersionLevelInfo(23, 30), new VersionLevelInfo(34, 24)}),  // 22
        new VersionInfo(28, 24, 1364, 0x177ec, new VersionLevelInfo[]{new VersionLevelInfo(9, 30), new VersionLevelInfo(18, 28), new VersionLevelInfo(25, 30), new VersionLevelInfo(30, 30)}),  // 23
        new VersionInfo(26, 26, 1474, 0x18ec4, new VersionLevelInfo[]{new VersionLevelInfo(10, 30), new VersionLevelInfo(20, 28), new VersionLevelInfo(27, 30), new VersionLevelInfo(32, 30)}), // 24
        new VersionInfo(30, 26, 1588, 0x191e1, new VersionLevelInfo[]{new VersionLevelInfo(12, 26), new VersionLevelInfo(21, 28), new VersionLevelInfo(29, 30), new VersionLevelInfo(35, 30)}), // 25
        new VersionInfo(28, 28, 1706, 0x1afab, new VersionLevelInfo[]{new VersionLevelInfo(12, 28), new VersionLevelInfo(23, 28), new VersionLevelInfo(34, 28), new VersionLevelInfo(37, 30)}), // 26
        new VersionInfo(32, 28, 1828, 0x1b08e, new VersionLevelInfo[]{new VersionLevelInfo(12, 30), new VersionLevelInfo(25, 28), new VersionLevelInfo(34, 30), new VersionLevelInfo(40, 30)}), // 27
        new VersionInfo(24, 24, 1921, 0x1cc1a, new VersionLevelInfo[]{new VersionLevelInfo(13, 30), new VersionLevelInfo(26, 28), new VersionLevelInfo(35, 30), new VersionLevelInfo(42, 30)}), // 28
        new VersionInfo(28, 24, 2051, 0x1d33f, new VersionLevelInfo[]{new VersionLevelInfo(14, 30), new VersionLevelInfo(28, 28), new VersionLevelInfo(38, 30), new VersionLevelInfo(45, 30)}), // 29
        new VersionInfo(24, 26, 2185, 0x1ed75, new VersionLevelInfo[]{new VersionLevelInfo(15, 30), new VersionLevelInfo(29, 28), new VersionLevelInfo(40, 30), new VersionLevelInfo(48, 30)}), // 30
        new VersionInfo(28, 26, 2323, 0x1f250, new VersionLevelInfo[]{new VersionLevelInfo(16, 30), new VersionLevelInfo(31, 28), new VersionLevelInfo(43, 30), new VersionLevelInfo(51, 30)}), // 31
        new VersionInfo(32, 26, 2465, 0x209d5, new VersionLevelInfo[]{new VersionLevelInfo(17, 30), new VersionLevelInfo(33, 28), new VersionLevelInfo(45, 30), new VersionLevelInfo(54, 30)}), // 32
        new VersionInfo(28, 28, 2611, 0x216f0, new VersionLevelInfo[]{new VersionLevelInfo(18, 30), new VersionLevelInfo(35, 28), new VersionLevelInfo(48, 30), new VersionLevelInfo(57, 30)}), // 33
        new VersionInfo(32, 28, 2761, 0x228ba, new VersionLevelInfo[]{new VersionLevelInfo(19, 30), new VersionLevelInfo(37, 28), new VersionLevelInfo(51, 30), new VersionLevelInfo(60, 30)}), // 34
        new VersionInfo(28, 24, 2876, 0x2379f, new VersionLevelInfo[]{new VersionLevelInfo(19, 30), new VersionLevelInfo(38, 28), new VersionLevelInfo(53, 30), new VersionLevelInfo(63, 30)}), // 35
        new VersionInfo(22, 26, 3034, 0x24b0b, new VersionLevelInfo[]{new VersionLevelInfo(20, 30), new VersionLevelInfo(40, 28), new VersionLevelInfo(56, 30), new VersionLevelInfo(66, 30)}), // 36
        new VersionInfo(26, 26, 3196, 0x2542e, new VersionLevelInfo[]{new VersionLevelInfo(21, 30), new VersionLevelInfo(43, 28), new VersionLevelInfo(59, 30), new VersionLevelInfo(70, 30)}), // 37
        new VersionInfo(30, 26, 3362, 0x26a64, new VersionLevelInfo[]{new VersionLevelInfo(22, 30), new VersionLevelInfo(45, 28), new VersionLevelInfo(62, 30), new VersionLevelInfo(74, 30)}), // 38
        new VersionInfo(24, 28, 3532, 0x27541, new VersionLevelInfo[]{new VersionLevelInfo(24, 30), new VersionLevelInfo(47, 28), new VersionLevelInfo(65, 30), new VersionLevelInfo(77, 30)}), // 39
        new VersionInfo(28, 28, 3706, 0x28c69, new VersionLevelInfo[]{new VersionLevelInfo(25, 30), new VersionLevelInfo(49, 28), new VersionLevelInfo(68, 30), new VersionLevelInfo(81, 30)}), // 40
    };

    private int version;

    public Version(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSize() {
        if(version <= 9)
            return 0;
        if(version <= 26)
            return 1;

        return 2;
    }

    // returns the number of data bytes that can be
    // stored in a QR code with the given version and level.
    public int dataBytes(Level level) {
        VersionInfo versionInfo = VERSION_INFOS[version];
        VersionLevelInfo levelInfo = versionInfo.levelInfos[level.ordinal()];

        return versionInfo.bytes - levelInfo.numberOfBlocks*levelInfo.numberOfCheckBytesPerBlock;
    }

    @Override
    public String toString() {
        return Integer.toString(version);
    }

    public static final class VersionLevelInfo {
        public int numberOfBlocks;
        public int numberOfCheckBytesPerBlock;

        public VersionLevelInfo(int numberOfBlocks, int numberOfCheckBytesPerBlock) {
            this.numberOfBlocks = numberOfBlocks;
            this.numberOfCheckBytesPerBlock = numberOfCheckBytesPerBlock;
        }
    }

    public static final class VersionInfo {
        public int apos;
        public int stride;
        public int bytes;
        public int pattern;
        public VersionLevelInfo[] levelInfos;

        public VersionInfo(int apos, int stride, int bytes, int pattern, VersionLevelInfo[] levelInfos) {
            this.apos = apos;
            this.stride = stride;
            this.bytes = bytes;
            this.pattern = pattern;
            this.levelInfos = levelInfos;
        }
    }
}
