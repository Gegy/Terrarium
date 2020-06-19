package net.gegy1000.terrarium.server.command;

import com.google.common.base.Strings;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public final class TerrariumCommand extends CommandBase {
    private static final Path DEBUG_ROOT = Paths.get("mods/terrarium/debug");
    private static final long PROFILE_TIME_SECONDS = 60;

    @Override
    public String getName() {
        return "terrarium";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "terrarium profile";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            String subCommand = args[0];

            if (subCommand.equals("profile")) {
                this.profile(server, sender);
                return;
            }
        }

        throw new WrongUsageException(this.getUsage(sender));
    }

    private void profile(MinecraftServer server, ICommandSender sender) {
        ThreadedProfiler.start();

        sender.sendMessage(new TextComponentString("Profiling Terrarium for " + PROFILE_TIME_SECONDS + " seconds"));

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(PROFILE_TIME_SECONDS * 1000);
            } catch (InterruptedException e) {
                Terrarium.LOGGER.warn("Interrupted while waiting for profiler", e);
            }

            server.addScheduledTask(() -> {
                List<ThreadedProfiler.Node> roots = ThreadedProfiler.stop();
                try {
                    this.writeResults(roots);
                    sender.sendMessage(new TextComponentString("Wrote profiler results to file"));
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Failed to write profiler results", e);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void writeResults(List<ThreadedProfiler.Node> roots) throws IOException {
        Path path = DEBUG_ROOT.resolve("profiler.txt");
        Files.createDirectories(path.getParent());

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
            this.writeResults(writer::println, roots);
        }
    }

    private void writeResults(Consumer<String> writer, List<ThreadedProfiler.Node> roots) {
        writer.accept("Profile results over " + PROFILE_TIME_SECONDS + " seconds:");

        for (ThreadedProfiler.Node root : roots) {
            this.writeNode(writer, root, 0);
            writer.accept("");
        }
    }

    private void writeNode(Consumer<String> writer, ThreadedProfiler.Node node, int depth) {
        long timeMs = node.time / 1000000;
        if (timeMs == 0) {
            return;
        }

        String indent = Strings.repeat("  ", depth);
        writer.accept(indent + node.name + ": " + timeMs + "ms");

        for (ThreadedProfiler.Node child : node.children.values()) {
            this.writeNode(writer, child, depth + 1);
        }
    }
}
