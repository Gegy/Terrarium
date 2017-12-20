package net.gegy1000.terrarium.server.map.source

enum class LoadingState(val languageKey: String, val textureY: Int) {
    LOADING_CACHED("state.cached", 0),
    LOADING_ONLINE("state.online", 10),
    LOADING_NO_CONNECTION("state.no_connection", 20)
}
