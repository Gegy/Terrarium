package net.gegy1000.terrarium.server.map.source.tiled

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.DataSource
import net.gegy1000.terrarium.server.map.source.LoadingState
import net.gegy1000.terrarium.server.map.source.LoadingStateHandler
import java.util.concurrent.TimeUnit

abstract class TiledSource<out T : TiledDataAccess>(val tileSize: Double, tileCacheSize: Int) : DataSource {
    constructor(tileSize: Int, tileCacheSize: Int): this(tileSize.toDouble(), tileCacheSize)

    private val tileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .maximumSize(tileCacheSize.toLong())
            .build(object : CacheLoader<DataTilePos, T>() {
                override fun load(pos: DataTilePos): T {
                    try {
                        return loadTile(pos) ?: defaultTile
                    } catch (e: Exception) {
                        LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION)
                        Terrarium.LOGGER.error("Failed to load tile at $pos", e)
                        return defaultTile
                    }
                }
            })

    abstract val defaultTile: T

    fun getTile(pos: DataTilePos) = tileCache[pos]!!

    abstract fun loadTile(pos: DataTilePos): T?
}
