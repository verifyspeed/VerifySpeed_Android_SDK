package co.verifyspeed.example.android.views.data;

import java.util.Objects;

public class VerificationKeyModel {
    private String verificationKey;
    private String deepLink;

    public VerificationKeyModel(String verificationKey, String deepLink) {
        this.verificationKey = verificationKey;
        this.deepLink = deepLink;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerificationKeyModel that = (VerificationKeyModel) o;
        return Objects.equals(verificationKey, that.verificationKey) &&
               Objects.equals(deepLink, that.deepLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verificationKey, deepLink);
    }
}
