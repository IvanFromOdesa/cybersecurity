package encryption.commons.crypt;

/**
 * Wrapper class for exchanging and generating key data.
 */
public abstract class KeyRqData<RD> {
    protected RD toSend;

    public KeyRqData(RD toSend) {
        this.toSend = toSend;
    }

    public RD getToSend() {
        return toSend;
    }

    public void setToSend(RD toSend) {
        this.toSend = toSend;
    }
}
