package encryption.rearrange;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class GlobalConfiguration {
    private static final Integer[] ORDER = new Integer[] {2, 5, 4, 1, 3};
    public static final int MIN_DEGREE = 10;
    public static final int MAX_DEGREE = 30;
    // Ensure the declared order
    public static final SimpleEncryption STANDARD_KEY = new SimpleEncryption((short) ORDER.length, new LinkedHashSet<>(Arrays.asList(ORDER)));
    public static final int PORT = 8080;
    public static final String IP = "127.0.0.1";
    public static final String STOP_WORD = "!stop";
    public static final String SET_KEY = "!key";
    public static final String RANDOM_KEY = "!rkey";
    public static final String ERROR_KEY = "!error";
    public static final String SUCCESS = "!acc";
    public static final String META_DATA = "!meta";
    public static final String WHITESPACE = " ";
    public static final String DOLLAR_SIGN = "$";
    public static final String HASH_SIGN = "#";
    public static final String DIVIDE_SIGN = "/";
    public static final String EXCLAMATION_MARK = "!";
    public static final Set<String> AVAILABLE_COMMANDS = new HashSet<>(Arrays.asList(
            STOP_WORD, SET_KEY, RANDOM_KEY
    ));
}
