package net.gegy1000.terrarium.server.world

interface HeightProvider {
    fun provideHeight(x: Int, z: Int): Int
}