package encryption.cesarcypher;

import encryption.commons.PropertiesLoader;

import java.util.Map;
import java.util.TreeMap;

public class Main {

    private static final String KEY_WORD = "DIPLOMAT";
    private static final int K = 10;

    public static void main(String[] args) {

        System.out.println(PropertiesLoader.class.getClassLoader().getResource(".properties"));

        /*Map<Integer, Character> upperMap = new TreeMap<>();
        Map<Character, Character> lowerMap = new TreeMap<>();

        fillKv(upperMap);

        int lower = K;
        for (int i = 0; i < KEY_WORD.length(); i ++) {
            char c = KEY_WORD.charAt(i);
            lowerMap.put(upperMap.get(lower), c);
            lower++;
        }

        if (KEY_WORD.chars().distinct().count() != KEY_WORD.length()) {
            throw new IllegalArgumentException("All chars must be distinct");
        }

        for (int upper = 0; upper < upperMap.size(); upper ++) {
            Character upValue = upperMap.get(upper);
            if (KEY_WORD.indexOf(upValue) == -1) {
                Character upKey = upperMap.get(lower);
                lowerMap.put(upKey, upValue);
                lower ++;
                if (lower == upperMap.size()) {
                    lower = 0;
                }
            }
        }

        System.out.println(lowerMap);*/
        /*lowerMap.put(upperMap.get(K), );*/
    }

    private static void fillKv(Map<Integer, Character> kv) {
        int order = 0;
        for (char ch = 'A'; ch <= 'Z'; ch ++) {
            kv.put(order, ch);
            order ++;
        }
    }
}
