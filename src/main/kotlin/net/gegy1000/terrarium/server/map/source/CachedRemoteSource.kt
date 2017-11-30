package net.gegy1000.terrarium.server.map.source

import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.gegy1000.terrarium.Terrarium
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

interface CachedRemoteSource {
    companion object {
        val globalCacheRoot = File(".", "mods/terrarium/cache/")

        val cacheService = Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("Terrarium Cache Service").setDaemon(true).build())
    }

    val cacheRoot: File

    fun getStream(key: DataTilePos): InputStream {
        val cachedFile = File(cacheRoot, getCachedName(key))
        if (!shouldLoadCache(key, cachedFile)) {
            try {
                val remoteData = IOUtils.toByteArray(getRemoteStream(key))
                cacheData(key, cachedFile, remoteData)
                return ByteArrayInputStream(remoteData)
            } catch (e: IOException) {
                Terrarium.LOGGER.info("Failed to load tile data stream at $key", e)
            }
        }
        return GZIPInputStream(FileInputStream(cachedFile))
    }

    private fun cacheData(key: DataTilePos, file: File, remoteData: ByteArray) {
        cacheService.submit {
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs()
            }
            val output = GZIPOutputStream(FileOutputStream(file))
            output.use { it.write(remoteData) }
            cacheMetadata(key)
        }
    }

    fun cacheMetadata(key: DataTilePos) {}

    fun shouldLoadCache(key: DataTilePos, file: File) = file.exists()

    fun getRemoteStream(key: DataTilePos): InputStream

    fun getCachedName(key: DataTilePos): String
}
