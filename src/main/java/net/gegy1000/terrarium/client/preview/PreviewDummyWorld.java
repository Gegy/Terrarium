package net.gegy1000.terrarium.client.preview;

import com.mojang.datafixers.types.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.minecraft.block.Block;
import net.minecraft.class_3689;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.client.world.DummyWorldSaveHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tag.TagManager;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

@Environment(EnvType.CLIENT)
public class PreviewDummyWorld extends World {
    private static final DummyWorldSaveHandler SAVE_HANDLER = new DummyWorldSaveHandler();
    private final ChunkGenerator<?> generator;

    public PreviewDummyWorld(LevelGeneratorType generatorType, GenerationSettings settings) {
        super(
                SAVE_HANDLER,
                new PersistentStateManager(SAVE_HANDLER),
                new LevelProperties(createInfo(generatorType, settings), ""),
                DimensionType.OVERWORLD,
                (world, dimension) -> new PreviewChunkManager(),
                new class_3689(() -> 0),
                false
        );
        this.generator = this.dimension.createChunkGenerator();
    }

    private static LevelInfo createInfo(LevelGeneratorType generatorType, GenerationSettings settings) {
        LevelInfo levelInfo = new LevelInfo(0, GameMode.ADVENTURE, false, false, generatorType);
        levelInfo.method_8579(settings.serialize(JsonOps.INSTANCE).getValue());
        return levelInfo;
    }

    public ChunkGenerator<?> getGenerator() {
        return this.generator;
    }

    @Override
    public TickScheduler<Block> getBlockTickScheduler() {
        return new DummyClientTickScheduler<>();
    }

    @Override
    public TickScheduler<Fluid> getFluidTickScheduler() {
        return new DummyClientTickScheduler<>();
    }

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public RecipeManager getRecipeManager() {
        return new RecipeManager();
    }

    @Override
    public TagManager getTagManager() {
        return new TagManager();
    }

    private static class PreviewChunkManager extends ChunkManager {
        @Nullable
        @Override
        public Chunk getChunkSync(int x, int z, ChunkStatus status, boolean var4) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void tick(BooleanSupplier var1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getStatus() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChunkGenerator<?> getChunkGenerator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LightingProvider getLightingProvider() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockView getWorldAsView() {
            throw new UnsupportedOperationException();
        }
    }
}
