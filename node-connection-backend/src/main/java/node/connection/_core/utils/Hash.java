package node.connection._core.utils;

import java.time.Instant;

public class Hash {

    public static final int DEFAULT_LENGTH = 8;

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        StringBuilder builder = new StringBuilder(length);
        long currentTimeMillis = Instant.now().toEpochMilli();

        for (int i = 0; i < length; i++) {
            int randomCharIndex = (int) (currentTimeMillis % 62);
            builder.append(getCharForIndex(randomCharIndex));
            currentTimeMillis /= 62;
        }

        return builder.toString();
    }

    private static char getCharForIndex(int index) {
        if (index < 10) {
            return (char) ('0' + index);
        } else if (index < 36) {
            return (char) ('A' + (index - 10));
        } else {
            return (char) ('a' + (index - 36));
        }
    }
}