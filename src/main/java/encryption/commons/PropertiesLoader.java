package encryption.commons;

import encryption.commons.log.MessageLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class PropertiesLoader {
    private static final URL PATH = PropertiesLoader.class.getClassLoader().getResource(".properties");
    private static final String SEPARATOR = "=";

    public static Map<String, String> readProperties() {
        String actualPath = String.valueOf(PATH);
        String toRemove = "file:" + System.lineSeparator();
        return readProperties(actualPath.substring(actualPath.indexOf(toRemove) + toRemove.length()));
    }

    public static Map<String, String> readProperties(String path) {
        Map<String, String> res = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(path)), 4 * 1024)) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (!s.contains(SEPARATOR) || s.startsWith(SEPARATOR) || s.endsWith(SEPARATOR)) {
                    continue;
                }
                String[] properties = s.split(SEPARATOR);
                res.put(properties[0].trim(), properties[1].trim());
            }
        } catch (Exception e) {
            MessageLogger.error(e);
        }
        return res;
    }
}
