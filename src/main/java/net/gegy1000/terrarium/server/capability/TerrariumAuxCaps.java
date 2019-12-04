package net.gegy1000.terrarium.server.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.ArrayList;
import java.util.Collection;

public interface TerrariumAuxCaps extends ICapabilityProvider {
    void addAux(ICapabilityProvider provider);

    class Implementation implements TerrariumAuxCaps {
        private final Collection<ICapabilityProvider> capabilities = new ArrayList<>();

        @Override
        public void addAux(ICapabilityProvider provider) {
            this.capabilities.add(provider);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            if (capability == TerrariumCapabilities.aux()) return true;
            return this.capabilities.stream().anyMatch(p -> p.hasCapability(capability, facing));
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == TerrariumCapabilities.aux()) {
                return TerrariumCapabilities.aux().cast(this);
            }
            for (ICapabilityProvider provider : this.capabilities) {
                T provided = provider.getCapability(capability, facing);
                if (provided != null) {
                    return provided;
                }
            }
            return null;
        }
    }
}
