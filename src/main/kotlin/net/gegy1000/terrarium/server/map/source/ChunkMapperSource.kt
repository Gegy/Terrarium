package net.gegy1000.terrarium.server.map.source

import net.gegy1000.terrarium.Terrarium
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.zip.Inflater

open class ChunkMapperSource {
    companion object {
        val CACHE_DIRECTORY = File(".", "mods/terrarium/cache/")

        val BUCKETS = HashMap<String, String>()

        val ADMIN: String
            get() = BUCKETS["chunkmapper-admin"] ?: "http://chunkmapper-admin.s3-website-us-east-1.amazonaws.com"

        val HEIGHTS_2: String
            get() = BUCKETS["chunkmapper-heights2"] ?: "http://chunkmapper-heights2.s3-website-us-east-1.amazonaws.com"

        val MAT: String
            get() = BUCKETS["chunkmapper-mat"] ?: "http://chunkmapper-mat.s3-website-us-east-1.amazonaws.com"

        suspend fun loadBuckets() {
            val url = URL("http://chunkmapper-admin.s3-website-us-east-1.amazonaws.com/buckets.txt")
            val input = BufferedReader(InputStreamReader(url.openStream()))
            try {
                input.lines().forEach {
                    val split = it.split(" ")
                    if (split.size == 2) {
                        val key = split[0]
                        val value = split[1]
                        BUCKETS[key] = value
                    }
                }
            } catch (e: IOException) {
                Terrarium.LOGGER.error("Failed to load ChunkMapper buckets", e)
            } finally {
                input.close()
            }
        }
    }

    protected fun inflate(input: InputStream): InputStream {
        input.use {
            val dataInput = DataInputStream(BufferedInputStream(input))

            val inflatedLength = dataInput.readInt()
            val compressedLength = dataInput.readInt()

            val compressed = ByteArray(compressedLength)
            val inflated = ByteArray(inflatedLength)

            dataInput.readFully(compressed)

            val inflater = Inflater()
            inflater.setInput(compressed)
            inflater.inflate(inflated)
            inflater.end()

            return ByteArrayInputStream(inflated)
        }
    }
}
