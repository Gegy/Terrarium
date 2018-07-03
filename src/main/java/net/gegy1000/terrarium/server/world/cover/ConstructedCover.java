package net.gegy1000.terrarium.server.world.cover;

public final class ConstructedCover<T extends CoverGenerationContext> {
    private final CoverType<T> coverType;
    private final T context;

    public ConstructedCover(CoverType<T> coverType, T context) {
        this.coverType = coverType;
        this.context = context;
    }

    public CoverType<T> getType() {
        return this.coverType;
    }

    public T getContext() {
        return this.context;
    }

    public CoverSurfaceGenerator<T> createSurfaceGenerator() {
        return this.coverType.createSurfaceGenerator(this.context);
    }

    public CoverDecorationGenerator<T> createDecorationGenerator() {
        return this.coverType.createDecorationGenerator(this.context);
    }
}
