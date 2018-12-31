package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(SlimeEntity.class)
public class MixinSlimeEntity {
    @Inject(method = "canSpawn", at = @At("HEAD"), cancellable = true)
    private void canSpawn(IWorld world, SpawnType spawnType, CallbackInfoReturnable<Boolean> callback) {
        Random random = ((SlimeEntity) (Object) this).getRand();
        if (this.shouldReduceSlimeSpawns(world, random)) {
            if (random.nextInt(4) != 0) {
                callback.setReturnValue(false);
            }
        }
    }

    private boolean shouldReduceSlimeSpawns(IWorld world, Random random) {
        LevelGeneratorType generatorType = world.getLevelProperties().getGeneratorType();
        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(generatorType);
        if (customGenerator != null) {
            return customGenerator.shouldReduceSlimeSpawns(world, random);
        }
        return false;
    }
}
