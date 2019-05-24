package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.OpProgressWatcher;
import net.gegy1000.earth.server.util.ProcedureProgressWatcher;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID, value = Side.CLIENT)
public class SharedInitializingGui extends GuiScreen implements ProcedureProgressWatcher {
    private static final int BAR_HEIGHT = 14;

    private static final int BAR_BACKGROUND_COLOR = 0xFF000000;
    private static final int BAR_FILL_COLOR = 0xFF336622;

    private static Method actionPerformedMethod;

    private final Runnable onComplete;

    private final ProgressBar overallProgress = new ProgressBar("Progress");
    private ProgressBar opProgress;

    private boolean complete;
    private int completeTicks;

    static {
        try {
            actionPerformedMethod = ReflectionHelper.findMethod(GuiScreen.class, "actionPerformed", "func_146284_a", GuiButton.class);
        } catch (ReflectionHelper.UnableToFindMethodException e) {
            TerrariumEarth.LOGGER.warn("Failed to find actionPerformed method", e);
        }
    }

    public SharedInitializingGui(Runnable onComplete) {
        this.onComplete = onComplete;
        SharedEarthData.initialize(this);
    }

    @SubscribeEvent
    public static void onButtonPress(GuiScreenEvent.ActionPerformedEvent event) {
        if (SharedEarthData.isInitialized()) {
            return;
        }

        GuiScreen gui = event.getGui();

        if (gui instanceof GuiCreateWorld && event.getButton().id == 0) {
            onCreateWorldPressed(event.getButton(), gui);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (SharedEarthData.isInitialized()) {
            return;
        }

        GuiScreen gui = event.getGui();

        if (gui instanceof GuiWorldSelection) {
            GuiListWorldSelection list = ((GuiWorldSelection) gui).selectionList;
            List<GuiListWorldSelectionEntry> entries = list.entries;
            for (int i = 0; i < entries.size(); i++) {
                GuiListWorldSelectionEntry entry = entries.get(i);
                entries.set(i, new HookedWorldSelectionEntry(list, entry));
            }
        }
    }

    private static void onCreateWorldPressed(GuiButton button, GuiScreen gui) {
        gui.mc.displayGuiScreen(new SharedInitializingGui(() -> {
            gui.mc.displayGuiScreen(gui);
            if (actionPerformedMethod != null) {
                try {
                    actionPerformedMethod.invoke(gui, button);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    TerrariumEarth.LOGGER.warn("Failed to invoke actionPerformed", e);
                }
            }
        }));
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.complete) {
            if (this.completeTicks++ > 10) {
                this.complete = false;
                this.opProgress = null;
                this.onComplete.run();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        String title = I18n.format("gui.earth.preparing");
        this.drawCenteredString(this.fontRenderer, title, centerX, 20, 0xFFFFFF);

        String[] description = new String[] {
                I18n.format("gui.earth.preparing.desc.1"),
                I18n.format("gui.earth.preparing.desc.2")
        };

        int descriptionY = 50;
        int descriptionLineSpacing = this.fontRenderer.FONT_HEIGHT + 2;

        for (int index = 0; index < description.length; index++) {
            String line = description[index];
            int lineY = descriptionY + index * descriptionLineSpacing;
            this.drawCenteredString(this.fontRenderer, line, centerX, lineY, 0xA0A0A0);
        }

        this.drawProgressBar(this.overallProgress, centerY);

        if (this.opProgress != null) {
            this.drawProgressBar(this.opProgress, centerY + 34);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawProgressBar(ProgressBar bar, int y) {
        int barWidth = MathHelper.ceil(this.width * 0.75);

        int minX = (this.width - barWidth) / 2;
        int minY = y - BAR_HEIGHT / 2;

        int maxX = (this.width + barWidth) / 2;
        int maxY = y + BAR_HEIGHT / 2;

        Gui.drawRect(minX, minY, maxX, maxY, BAR_BACKGROUND_COLOR);

        if (bar.progress > 0.0) {
            int progressX = MathHelper.floor(minX + (maxX - minX) * bar.progress);
            Gui.drawRect(minX + 1, minY + 1, progressX - 1, maxY - 1, BAR_FILL_COLOR);
        }

        int centerX = (minX + maxX) / 2;

        int titleY = minY - this.fontRenderer.FONT_HEIGHT - 2;
        int centerY = (minY + maxY - this.fontRenderer.FONT_HEIGHT) / 2 + 1;

        String percentageString = String.format("%.0f%%", bar.progress * 100.0);
        this.drawCenteredString(this.fontRenderer, percentageString, centerX, centerY, 0xA0A0A0);

        this.drawCenteredString(this.fontRenderer, bar.description, centerX, titleY, 0xA0A0A0);
    }

    @Override
    public OpProgressWatcher startOp(String description) {
        this.opProgress = new ProgressBar(description);
        return this.opProgress;
    }

    @Override
    public void notifyProgress(double percentage) {
        this.overallProgress.progress = percentage;
    }

    @Override
    public void notifyComplete() {
        this.complete = true;
        this.overallProgress.progress = 1.0;
    }

    private static class ProgressBar implements OpProgressWatcher {
        final String description;
        double progress;

        ProgressBar(String description) {
            this.description = description;
        }

        @Override
        public void notifyProgress(double percentage) {
            this.progress = percentage;
        }

        @Override
        public void notifyComplete() {
            this.progress = 1.0;
        }

        @Override
        public void notifyException(Exception e) {
            // TODO
        }
    }
}
