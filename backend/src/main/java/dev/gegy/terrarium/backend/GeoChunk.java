package dev.gegy.terrarium.backend;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GeoChunk {
    public static final Codec<GeoChunk> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<GeoChunk, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map -> {
                final Reference2ObjectMap<GeoAttachment<?>, Object> read = new Reference2ObjectOpenHashMap<>();
                final DataResult<Unit> result = map.entries().reduce(
                        DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
                        (r, pair) -> {
                            final DataResult<GeoAttachment<?>> keyResult = GeoAttachment.CODEC.parse(ops, pair.getFirst());
                            return keyResult.flatMap(key -> {
                                final DataResult<Pair<GeoAttachment<?>, Object>> entry = key.codec().parse(ops, pair.getSecond())
                                        .map(value -> Pair.of(key, value));
                                return r.apply2stable((u, p) -> {
                                    read.put(p.getFirst(), p.getSecond());
                                    return u;
                                }, entry);
                            });
                        },
                        (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
                );
                return result.map(unit -> Pair.of(new GeoChunk(read), input));
            });
        }

        @Override
        public <T> DataResult<T> encode(final GeoChunk input, final DynamicOps<T> ops, final T prefix) {
            final RecordBuilder<T> map = ops.mapBuilder();
            for (final Map.Entry<GeoAttachment<?>, Object> entry : input.attachmentMap.entrySet()) {
                final GeoAttachment<?> key = entry.getKey();
                final Object value = entry.getValue();
                map.add(GeoAttachment.CODEC.encodeStart(ops, key), encodeUnchecked(ops, key, value));
            }
            return map.build(prefix);
        }

        @SuppressWarnings("unchecked")
        private static <T, V> DataResult<T> encodeUnchecked(final DynamicOps<T> ops, final GeoAttachment<V> key, final Object value) {
            return key.codec().encodeStart(ops, (V) value);
        }
    };

    public static final GeoChunk EMPTY = new GeoChunk(Reference2ObjectMaps.emptyMap());

    private final Reference2ObjectMap<GeoAttachment<?>, Object> attachmentMap;

    private GeoChunk(final Reference2ObjectMap<GeoAttachment<?>, Object> attachmentMap) {
        this.attachmentMap = attachmentMap;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <V> V get(final GeoAttachment<V> attachment) {
        return (V) attachmentMap.get(attachment);
    }

    public boolean isEmpty() {
        return attachmentMap.isEmpty();
    }

    public static class Builder {
        private final Map<GeoAttachment<?>, CompletableFuture<? extends Optional<?>>> attachmentMap = new Reference2ObjectOpenHashMap<>();

        public <V> Builder put(final GeoAttachment<V> attachment, final CompletableFuture<Optional<V>> value) {
            attachmentMap.put(attachment, value);
            return this;
        }

        public CompletableFuture<Optional<GeoChunk>> build() {
            return CompletableFuture.allOf(attachmentMap.values().toArray(CompletableFuture[]::new)).thenApply(unused -> {
                final Reference2ObjectMap<GeoAttachment<?>, Object> result = new Reference2ObjectOpenHashMap<>(attachmentMap.size());
                attachmentMap.forEach((attachment, future) ->
                        future.join().ifPresent(value -> result.put(attachment, value))
                );
                return Optional.of(new GeoChunk(result));
            });
        }
    }
}
