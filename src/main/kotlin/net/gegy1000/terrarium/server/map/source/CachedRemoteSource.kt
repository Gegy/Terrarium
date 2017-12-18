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
            val remoteStream = getRemoteStream(key)
            try {
                val remoteData = IOUtils.toByteArray(remoteStream)
                cacheData(key, cachedFile, remoteData)
                return ByteArrayInputStream(remoteData)
            } catch (e: IOException) {
                Terrarium.LOGGER.info("Failed to load remote tile data stream at $key", e)
            } finally {
                remoteStream.close()
            }
        }

        try {
            return GZIPInputStream(FileInputStream(cachedFile)).buffered()
        } catch (e: IOException) {
            Terrarium.LOGGER.info("Failed to load local tile data stream at $key", e)
        }

        return ByteArrayInputStream(byteArrayOf())
    }

    private fun cacheData(key: DataTilePos, file: File, remoteData: ByteArray) {
        cacheService.submit {
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs()
            }
            try {
                val output = GZIPOutputStream(FileOutputStream(file))
                output.use { it.write(remoteData) }
                cacheMetadata(key)
            } catch (e: Exception) {
                Terrarium.LOGGER.error("Failed to cache tile at $key to $file", e)
            }
        }
    }

    fun cacheMetadata(key: DataTilePos) {}

    fun shouldLoadCache(key: DataTilePos, file: File) = file.exists()

    fun getRemoteStream(key: DataTilePos): InputStream

    fun getCachedName(key: DataTilePos): String
}
