package net.gegy1000.earth.server.world.data;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.Base64;

public class EarthApiKeys {
    @SerializedName("geocoder_key")
    private final String geocoderKey = "";
    @SerializedName("autocomplete_key")
    private final String autocompleteKey = "";
    @SerializedName("streetview_key")
    private final String streetviewKey = "";

    @Nullable
    public String getGeocoderKey() {
        return this.decode(this.geocoderKey, 31);
    }

    @Nullable
    public String getAutocompleteKey() {
        return this.decode(this.autocompleteKey, 961);
    }

    @Nullable
    public String getStreetviewKey() {
        return this.decode(this.streetviewKey, 729);
    }

    @Nullable
    private String decode(String encoded, int shift) {
        if (Strings.isNullOrEmpty(encoded)) return null;

        byte[] encodedKeyBytes = Base64.getDecoder().decode(encoded);
        byte[] decodedBytes = new byte[encodedKeyBytes.length];
        for (int i = 0; i < encodedKeyBytes.length; i++) {
            decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - shift);
        }

        return new String(decodedBytes);
    }
}
