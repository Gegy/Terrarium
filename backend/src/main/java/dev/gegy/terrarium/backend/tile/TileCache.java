package dev.gegy.terrarium.backend.tile;

import dev.gegy.terrarium.backend.loader.Cacher;

public interface TileCache {
    <V> Cacher<TileKey, V> createCacher(TileMap<V> map);
}
