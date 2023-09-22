package encryption.permutation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Configuration {
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
    public static final String KEY_SPLITTER = ",";
    public static final Set<String> AVAILABLE_COMMANDS = new HashSet<>(Arrays.asList(
            STOP_WORD, SET_KEY, RANDOM_KEY, META_DATA
    ));
}
