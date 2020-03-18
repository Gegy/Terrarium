package net.gegy1000.earth.server.world.data;

import com.google.gson.annotations.SerializedName;

import java.util.Base64;

public class EarthApiKeys {
    @SerializedName("geocoder_key")
    private String geocoderKey = "";
    @SerializedName("autocomplete_key")
    private String autocompleteKey = "";
    @SerializedName("streetview_key")
    private String streetviewKey = "";

    public String getGeocoderKey() {
        byte[] encodedKeyBytes = Base64.getDecoder().decode(this.geocoderKey);
        byte[] decodedBytes = new byte[encodedKeyBytes.length];
        for (int i = 0; i < encodedKeyBytes.length; i++) {
            decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 31);
        }
        return new String(decodedBytes);
    }

    public String getAutocompleteKey() {
        byte[] encodedKeyBytes = Base64.getDecoder().decode(this.autocompleteKey);
        byte[] decodedBytes = new byte[encodedKeyBytes.length];
        for (int i = 0; i < encodedKeyBytes.length; i++) {
            decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 961);
        }
        return new String(decodedBytes);
    }

    public String getStreetviewKey() {
        byte[] encodedKeyBytes = Base64.getDecoder().decode(this.streetviewKey);
        byte[] decodedBytes = new byte[encodedKeyBytes.length];
        for (int i = 0; i < encodedKeyBytes.length; i++) {
            decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 729);
        }
        return new String(decodedBytes);
    }
}
