package net.gegy1000.terrarium.mixin;

import com.mojang.datafixers.Dynamic;
import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.server.world.GenerationContext;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldDimension.class)
public abstract class MixinOverworldDimension extends Dimension {
    public MixinOverworldDimension(World world, DimensionType dimensionType) {
        super(world, dimensionType);
    }

    @Inject(method = "createChunkGenerator", at = @At("HEAD"), cancellable = true)
    private void createChunkGenerator(CallbackInfoReturnable<ChunkGenerator<? extends ChunkGeneratorSettings>> callback) {
        LevelProperties levelProperties = this.world.getLevelProperties();
        LevelGeneratorType generatorType = levelProperties.getGeneratorType();
        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(generatorType);

        if (customGenerator instanceof TerrariumGeneratorType<?>) {
            TerrariumGeneratorType<?> terrariumGenerator = (TerrariumGeneratorType<?>) customGenerator;

            // TODO: Preview generation context?
            GenerationSettings settings = parseSettings(terrariumGenerator, levelProperties.getGeneratorOptions());
            callback.setReturnValue(terrariumGenerator.createGenerator(this.world, settings, GenerationContext.WORLD));
        }
    }

    private static GenerationSettings parseSettings(TerrariumGeneratorType<?> generatorType, CompoundTag settings) {
        PropertyPrototype prototype = generatorType.buildPropertyPrototype();
        GenerationSettings presetSettings = generatorType.getPreset().createSettings(prototype);
        if (settings == null || settings.isEmpty()) {
            return presetSettings;
        }
        return presetSettings.union(GenerationSettings.deserialize(prototype, new Dynamic<>(NbtOps.INSTANCE, settings)));
    }
}
