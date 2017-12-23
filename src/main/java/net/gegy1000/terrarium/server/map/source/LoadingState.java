package net.gegy1000.terrarium.server.map.source;

public enum LoadingState {
    LOADING_CACHED("state.cached", 0),
    LOADING_ONLINE("state.online", 10),
    LOADING_NO_CONNECTION("state.no_connection", 20);

    private final String languageKey;
    private final int textureY;

    LoadingState(String languageKey, int textureY) {
        this.languageKey = languageKey;
        this.textureY = textureY;
    }

    public String getLanguageKey() {
        return this.languageKey;
    }

    public int getTextureY() {
        return this.textureY;
    }
}
