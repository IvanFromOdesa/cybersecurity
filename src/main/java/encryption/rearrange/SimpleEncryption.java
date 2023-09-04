package encryption.rearrange;

import java.util.*;
import java.util.stream.Collectors;

public record SimpleEncryption(short degree, Set<Integer> sequence) {

    // TODO: we need to give additional info about the whitespace indexes and the length of initial string
    //  to clear garbage and add whitespaces back when decrypting
    // E.g. SAMPLE TWO -> LASEMrWTaO#10$6.
    // 10 - the length of the string,
    // 6 - index of a whitespace.
    // Probably need to encrypt this info either.
    public static class Encryptor {
        /**
         * Encrypts message using the order defined in the key.
         * <br/>
         * E.g. SAMPLE (3, 2, 5, 1, 4) -> LASEM.
         * <br/>
         * Adds garbage chars to the end of the string if needed.
         * @return encrypted string
         */
        public static String encrypt(String msg, SimpleEncryption key) {
            // Sample text.
            // TABLE -> 3, 4, 5, 1, 2
            List<String> subs = new ArrayList<>();
            short degree = key.degree();
            // Sampletext.
            msg = String.join("", msg.split(" "));
            // Sampletext.ewrq
            msg = addGarbageIfNeeded(msg, degree);
            // Sampl, etext, .ewrq
            divideIntoSubstrings(msg, subs, degree);

            List<String> result = subs.stream().map(sub -> {
                char[] chars = sub.toCharArray();
                Map<Integer, Character> res = new TreeMap<>();

                List<Integer> order = new ArrayList<>(key.sequence());

                for (int i = 0; i < chars.length; i++) {
                    int index = order.get(i);
                    char c = chars[i];
                    res.put(index, c);
                }

                // Sampl -> lpSam, etext -> txete, .ewrq -> qr.ew
                return res.values().stream().map(Object::toString).collect(Collectors.joining());
            }).toList();

            // -> lpSamtxeteqr.ew
            return String.join("", result);
        }

        private static String addGarbageIfNeeded(String msg, short degree) {
            int remains = msg.length() % degree;

            if (remains == 0) {
                return msg;
            }

            Random r = new Random();
            char c = (char) (r.nextInt(26) + 'a');

            msg += c;
            return addGarbageIfNeeded(msg, degree);
        }
    }

    public static class Decryptor {
        /*
         * Get the characters one by indexes.
         * MESPLA -> 3, 6, 1, 4, 5, 2
         * Start by getting the 3rd "S", then the 6th "A" etc.
         */
        public static String decrypt(String msg, SimpleEncryption key) {
            List<String> subs = new ArrayList<>();
            // lpSamtxeteqr.ew -> lpSam, txete, eqr.ew
            divideIntoSubstrings(msg, subs, key.degree());
            List<String> result = subs.stream().map(sub -> {
                List<Character> chars = sub.chars().mapToObj(c -> (char) c).toList();
                List<Integer> order = new ArrayList<>(key.sequence());
                StringBuilder res = new StringBuilder();
                for (Integer index : order) {
                    res.append(chars.get(index - 1));
                }
                // Sampl, etext, .ewrq
                return res.toString();
            }).toList();
            // -> Sampletext.ewrq
            return String.join("", result);
        }
    }

    private static void divideIntoSubstrings(String msg, List<String> subs, short degree) {
        for (int i = 0; i < msg.length(); i += degree) {
            subs.add(msg.substring(i, Math.min(msg.length(), i + degree)));
        }
    }
}
