package net.gegy1000.terrarium.server.world.pipeline.component;

import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.ParsableInstanceObject;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.pipeline.populator.RegionPopulator;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;
import net.minecraft.world.World;

public final class AttachedComponent<T> {
    private final RegionComponentType<T> type;
    private final RegionPopulator<T> populator;

    public AttachedComponent(RegionComponentType<T> type, RegionPopulator<T> populator) {
        this.type = type;
        this.populator = populator;
    }

    public RegionComponentType<T> getType() {
        return this.type;
    }

    public RegionComponent<T> createAndPopulate(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        T data = this.populator.populate(settings, pos, regionSize, width, height);
        return new RegionComponent<>(this.type, data);
    }

    public static class Parsable<T> {
        private final RegionComponentType<T> type;
        private final ParsableInstanceObject<RegionPopulator<T>> populatorParser;

        public Parsable(RegionComponentType<T> type, ParsableInstanceObject<RegionPopulator<T>> populatorParser) {
            this.type = type;
            this.populatorParser = populatorParser;
        }

        public RegionComponentType<T> getType() {
            return this.type;
        }

        @SuppressWarnings("unchecked")
        public AttachedComponent<T> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser) {
            RegionPopulator<?> populator = this.populatorParser.parse(worldData, world, valueParser);
            if (populator.getType() != this.type.getType()) {
                throw new JsonSyntaxException("Found populator of wrong data type " + populator.getType());
            }

            return new AttachedComponent<>(this.type, (RegionPopulator<T>) populator);
        }
    }
}
