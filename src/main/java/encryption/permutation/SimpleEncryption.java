package encryption.permutation;

import encryption.commons.crypt.BaseEncryption;
import encryption.commons.crypt.IDecryptor;
import encryption.commons.crypt.IEncryptor;
import encryption.commons.net.EntityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static encryption.permutation.GlobalConfiguration.*;

public class SimpleEncryption extends BaseEncryption {
    private final short degree;
    private final Set<Integer> sequence;
    // Contains idx of whitespaces and the length of the string
    private String metaData;

    public SimpleEncryption(short degree, Set<Integer> sequence) {
        this.degree = degree;
        this.sequence = sequence;
    }

    public static class Encryptor implements IEncryptor<SimpleEncryption> {
        /**
         * Encrypts message using the order defined in the key.
         * Adds garbage chars to the end of the string if needed.
         * @return encrypted string
         */
        @Override
        public String encrypt(String msg, SimpleEncryption key) {
            // Sample text.
            // TABLE -> 3, 4, 5, 1, 2
            List<String> subs = new ArrayList<>();
            short degree = key.degree();
            // Sampletext.
            msg = String.join("", msg.split(WHITESPACE));
            // Sampletext.ewrq
            msg = addGarbageIfNeeded(new StringBuilder(msg), degree);
            // Sampl, etext, .ewrq
            divideIntoSubstrings(msg, subs, degree);

            List<Integer> order = new ArrayList<>(key.sequence());

            List<String> result = subs.stream().map(sub -> {
                StringBuilder res = new StringBuilder();

                for (int i = 1; i <= sub.length(); i++) {
                    res.append(sub.charAt(order.indexOf(i)));
                }

                // Sampl -> lpSam, etext -> txete, .ewrq -> qr.ew
                return res.toString();
            }).toList();

            // -> lpSamtxeteqr.ew
            return String.join("", result);
        }

        public static String prepareMetaData(String msg) {
            StringBuilder res = new StringBuilder();
            int initialSize = msg.length();
            res.append(DOLLAR_SIGN);
            while (msg.contains(WHITESPACE)) {
                int idx = msg.indexOf(WHITESPACE);
                res.append(idx);
                res.append(DIVIDE_SIGN);
                msg = msg.replaceFirst(WHITESPACE, "");
            }
            res.append(HASH_SIGN).append(initialSize);
            return res.toString();
        }

        private static String addGarbageIfNeeded(StringBuilder sb, short degree) {
            int remains = sb.length() % degree;

            if (remains == 0) {
                return sb.toString();
            }

            Random r = new Random();
            char c = (char) (r.nextInt(26) + 'a');

            sb.append(c);
            return addGarbageIfNeeded(sb, degree);
        }
    }

    public static class Decryptor implements IDecryptor<SimpleEncryption> {
        /*
         * Get the characters one by indexes.
         * MESPLA -> 3, 6, 1, 4, 5, 2
         * Start by getting the 3rd "S", then the 6th "A" etc.
         */
        @Override
        public String decrypt(String msg, SimpleEncryption key) {
            List<String> subs = new ArrayList<>();
            // lpSamtxeteqr.ew -> lpSam, txete, eqr.ew
            divideIntoSubstrings(msg, subs, key.degree());
            List<Integer> order = new ArrayList<>(key.sequence());
            List<String> result = subs.stream().map(sub -> {
                StringBuilder res = new StringBuilder();
                for (Integer index : order) {
                    res.append(sub.charAt(index - 1));
                }
                // Sampl, etext, .ewrq
                return res.toString();
            }).toList();
            // -> Sampletext.ewrq
            String joined = String.join("", result);
            // -> Sample text.
            return key.isNullMetaData() ? joined : withMetaData(joined, key);
        }

        private static String withMetaData(String msg, SimpleEncryption key) {
            try {
                StringBuilder res = new StringBuilder(msg);
                String metaData = key.getMetaData();
                String whitespaces = metaData.substring(metaData.indexOf(DOLLAR_SIGN) + 1, metaData.lastIndexOf(HASH_SIGN));
                if (!"".equals(whitespaces)) {
                    AtomicInteger n = new AtomicInteger();
                    Arrays.stream(whitespaces.split(DIVIDE_SIGN)).mapToInt(Integer::parseInt).forEach(s -> res.insert(s + n.getAndIncrement(), WHITESPACE));
                }
                String s = metaData.substring(metaData.indexOf(HASH_SIGN) + 1);
                key.clearMetaData();
                return res.substring(0, Integer.parseInt(s));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return msg;
            }
        }
    }

    private static void divideIntoSubstrings(String msg, List<String> subs, short degree) {
        for (int i = 0; i < msg.length(); i += degree) {
            subs.add(msg.substring(i, Math.min(msg.length(), i + degree)));
        }
    }

    public static SimpleEncryption generateKey(EntityModel<SimpleEncryption> entity, String[] table) {
        Integer[] order;
        try {
            order = Arrays.stream(table).mapToInt(Integer::parseInt).boxed().toArray(Integer[]::new);
        } catch (NumberFormatException e) {
            System.err.println("Error generating a key.");
            return entity.getKey();
        }
        System.out.println("Key generated successfully: " + Arrays.toString(order));
        return new SimpleEncryption((short) order.length, new LinkedHashSet<>(Arrays.asList(order)));
    }

    public short degree() {
        return degree;
    }

    public Set<Integer> sequence() {
        return sequence;
    }

    public String getMetaData() {
        return metaData;
    }

    public boolean isNullMetaData() {
        return metaData == null || "".equals(metaData);
    }

    // After the message is successfully decrypted, we clear the metadata
    public void clearMetaData() {
        metaData = null;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleEncryption) obj;
        return this.degree == that.degree &&
                Objects.equals(this.sequence, that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(degree, sequence);
    }

    @Override
    public String toString() {
        return "SimpleEncryption[" +
                "degree=" + degree + ", " +
                "sequence=" + sequence + ']';
    }

}
