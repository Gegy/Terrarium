package net.gegy1000.terrarium.server.util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class BlockStateParser {
    public static Optional<IBlockState> parseBlockState(String string) {
        String[] tokens = string.split("#");

        ResourceLocation identifier = new ResourceLocation(tokens[0]);
        if (!Block.REGISTRY.containsKey(identifier)) {
            return Optional.empty();
        }

        Block block = Block.REGISTRY.getObject(identifier);
        if (tokens.length == 1) {
            return Optional.of(block.getDefaultState());
        }

        String[] properties = tokens[1].split(",");

        IBlockState state = block.getDefaultState();
        for (String propertyToken : properties) {
            String[] propertyTokens = propertyToken.split("=");
            if (propertyTokens.length != 2) {
                throw new IllegalArgumentException("Invalid property syntax for \"" + propertyToken + "\"");
            }

            String propertyName = propertyTokens[0];
            String propertyValue = propertyTokens[1];

            IProperty<?> property = getProperty(state, propertyName);
            state = setProperty(state, property, propertyValue);
        }

        return Optional.of(state);
    }

    private static IProperty<?> getProperty(IBlockState state, String propertyName) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        throw new IllegalArgumentException("Found no property with name \"" + propertyName + "\" on block \"" + state.getBlock() + "\"");
    }

    private static <T extends Comparable<T>> IBlockState setProperty(IBlockState state, IProperty<T> property, String value) {
        Optional<T> parsed = property.parseValue(value).toJavaUtil();
        if (!parsed.isPresent()) {
            throw new IllegalArgumentException("Invalid value \"" + value + "\" for property \"" + property.getName() + "\"");
        }
        return state.withProperty(property, parsed.get());
    }
}
