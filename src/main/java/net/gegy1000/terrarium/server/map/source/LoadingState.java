package net.gegy1000.terrarium.server.map.source;

public enum LoadingState {
    LOADING_CACHED("state.cached", 0, 1),
    LOADING_ONLINE("state.online", 10, 3),
    LOADING_NO_CONNECTION("state.no_connection", 20, 5);

    private final String languageKey;
    private final int textureY;
    private final int weight;

    LoadingState(String languageKey, int textureY, int weight) {
        this.languageKey = languageKey;
        this.textureY = textureY;
        this.weight = weight;
    }

    public String getLanguageKey() {
        return this.languageKey;
    }

    public int getTextureY() {
        return this.textureY;
    }

    public int getWeight() {
        return this.weight;
    }
}
