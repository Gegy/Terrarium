package net.gegy1000.earth.server.event;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.function.Function;

public final class InitBiomeClassifierEvent extends Event {
    private final GenerationSettings settings;
    private BiomeClassifier classifier;

    public InitBiomeClassifierEvent(GenerationSettings settings, BiomeClassifier classifier) {
        this.settings = settings;
        this.classifier = classifier;
    }

    public GenerationSettings getSettings() {
        return this.settings;
    }

    public void modifyClassifier(Function<BiomeClassifier, BiomeClassifier> op) {
        BiomeClassifier classifier = op.apply(this.classifier);
        this.classifier = Preconditions.checkNotNull(classifier, "classifier cannot be null");
    }

    public BiomeClassifier getClassifier() {
        return this.classifier;
    }
}
