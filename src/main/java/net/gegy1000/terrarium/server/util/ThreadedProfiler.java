package net.gegy1000.terrarium.server.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public final class ThreadedProfiler implements Profiler {
    private static Map<Thread, ThreadedProfiler> profilers;

    private final Deque<String> stack = new ArrayDeque<>();
    private final LongArrayList stackStartTime = new LongArrayList();
    private final Object2LongMap<String> accumulator = new Object2LongOpenHashMap<>();

    private final Handle handle = new Handle(this);

    private ThreadedProfiler() {
        this.accumulator.defaultReturnValue(0);
    }

    public static Profiler get() {
        if (profilers == null) {
            return Profiler.VOID;
        }

        Thread thread = Thread.currentThread();
        return profilers.computeIfAbsent(thread, t -> new ThreadedProfiler());
    }

    public static void start() {
        profilers = new Reference2ObjectOpenHashMap<>();
    }

    public static List<Node> stop() {
        Map<Thread, ThreadedProfiler> profilers = ThreadedProfiler.profilers;
        ThreadedProfiler.profilers = null;

        if (profilers == null || profilers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Node> nodes = new ArrayList<>(profilers.size());

        for (Map.Entry<Thread, ThreadedProfiler> entry : profilers.entrySet()) {
            Node root = new Node(entry.getKey().getName());

            ThreadedProfiler profiler = entry.getValue();
            profiler.collectTo(root);

            nodes.add(root);
        }

        return nodes;
    }

    @Override
    public Handle push(String name) {
        String path = name;
        if (!this.stack.isEmpty()) {
            path = this.stack.peek() + "/" + path;
        }

        this.stack.push(path);
        this.stackStartTime.push(System.nanoTime());

        return this.handle;
    }

    @Override
    public void pop() {
        long time = System.nanoTime();

        String path = this.stack.pop();
        long start = this.stackStartTime.popLong();

        long duration = time - start;
        long accumulator = this.accumulator.getLong(path);
        this.accumulator.put(path, accumulator + duration);
    }

    void collectTo(Node root) {
        for (Object2LongMap.Entry<String> entry : this.accumulator.object2LongEntrySet()) {
            String path = entry.getKey();

            Node node = root.getOrCreate(path);
            node.time = entry.getLongValue();
        }

        for (Node child : root.children.values()) {
            root.time += child.time;
        }
    }

    public static class Node {
        public final String name;
        public final Map<String, Node> children = new Object2ObjectOpenHashMap<>();
        public long time;

        Node(String name) {
            this.name = name;
        }

        Node getOrCreate(String path) {
            int idx = path.indexOf('/');
            if (idx == -1) {
                return this.children.computeIfAbsent(path, Node::new);
            }

            String root = path.substring(0, idx);
            String rest = path.substring(idx + 1);

            return this.children.computeIfAbsent(root, Node::new)
                    .getOrCreate(rest);
        }
    }
}
