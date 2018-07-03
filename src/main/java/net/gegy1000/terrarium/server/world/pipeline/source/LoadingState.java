package net.gegy1000.terrarium.server.world.pipeline.source;

public enum LoadingState {
    LOADING_CACHED("state.terrarium.cached", 0, 1, 2000),
    LOADING_ONLINE("state.terrarium.online", 10, 3, 2000),
    LOADING_NO_CONNECTION("state.terrarium.no_connection", 20, 5, 5000);

    private final String languageKey;
    private final int textureY;
    private final int weight;

    private final long lifetime;

    LoadingState(String languageKey, int textureY, int weight, long lifetime) {
        this.languageKey = languageKey;
        this.textureY = textureY;
        this.weight = weight;
        this.lifetime = lifetime;
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

    public long getLifetime() {
        return this.lifetime;
    }
}
