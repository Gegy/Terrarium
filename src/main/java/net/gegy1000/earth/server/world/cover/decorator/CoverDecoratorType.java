package net.gegy1000.earth.server.world.cover.decorator;

public final class CoverDecoratorType<T extends CoverDecorator> {
    private CoverDecoratorType() {
    }

    public static <T extends CoverDecorator> CoverDecoratorType<T> create() {
        return new CoverDecoratorType<>();
    }
}
