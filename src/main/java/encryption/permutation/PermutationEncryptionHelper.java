package encryption.permutation;

import encryption.commons.crypt.EncryptionHelper;
import encryption.commons.log.MessageLogger;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static encryption.permutation.Configuration.RANDOM_KEY;
import static encryption.permutation.Configuration.WHITESPACE;

public class PermutationEncryptionHelper implements EncryptionHelper<PermutationEncryption, PermutationRqData> {
    private static final int RANDOM_MIN_DEGREE = 10;
    private static final int RANDOM_MAX_DEGREE = 30;

    @Override
    public Optional<PermutationEncryption> getDefault(Map<String, String> properties) {
        String key = properties.get("permutation.key");
        Optional<PermutationEncryption> defaultKey = Optional.empty();
        if (key != null) {
            defaultKey = generateKey(new PermutationRqData(key));
        }
        return defaultKey;
    }

    @Override
    public Optional<PermutationEncryption> getRandomKey(PermutationRqData rqData) {
        List<String> res;
        Random random = new Random();
        int degree;
        String msg = rqData.getToSend();
        if (msg.matches(RANDOM_KEY + WHITESPACE + "\\d+")) {
            try {
                degree = Integer.parseInt(msg.replace(RANDOM_KEY + WHITESPACE, ""));
            } catch (NumberFormatException e) {
                MessageLogger.error(e);
                return Optional.empty();
            }
        } else {
            MessageLogger.warning("Generating with a random degree...");
            degree = random.nextInt(RANDOM_MIN_DEGREE, RANDOM_MAX_DEGREE + 1);
        }
        res = new LinkedList<>();
        while (res.size() != degree) {
            int idx = random.nextInt(1, degree + 1);
            String o = String.valueOf(idx);
            if (!res.contains(o)) {
                res.add(o);
            }
        }
        return generateKey(new PermutationRqData(String.join(WHITESPACE, res)));
    }

    @Override
    public Optional<PermutationEncryption> generateKey(PermutationRqData rqData) {
        Integer[] order;
        try {
            order = Arrays.stream(rqData.getToSend().split(WHITESPACE)).mapToInt(Integer::parseInt).boxed().toArray(Integer[]::new);
        } catch (Exception e) {
            MessageLogger.error("Error generating a key.", e);
            return Optional.empty();
        }
        MessageLogger.info("Key generated successfully: " + Arrays.toString(order));
        return Optional.of(new PermutationEncryption((short) order.length, new LinkedHashSet<>(Arrays.asList(order))));
    }
}
