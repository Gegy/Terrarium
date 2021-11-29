package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.toast.DataFailToast;
import net.gegy1000.terrarium.server.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    private static Method actionPerformed;
    private static Field selectedWorldType;
    private static Field structuresButton;

    static {
        try {
            selectedWorldType = ObfuscationReflectionHelper.findField(GuiCreateWorld.class, "field_146331_K");
        } catch (UnableToFindFieldException e) {
            Terrarium.LOGGER.warn("Failed to reflect selected world type", e);
        }

        try {
            structuresButton = ObfuscationReflectionHelper.findField(GuiCreateWorld.class, "field_146325_B");
        } catch (UnableToFindMethodException e) {
            Terrarium.LOGGER.warn("Failed to reflect structures button", e);
        }

        try {
            actionPerformed = ObfuscationReflectionHelper.findMethod(GuiScreen.class, "func_146284_a", void.class, GuiButton.class);
        } catch (UnableToFindMethodException e) {
            Terrarium.LOGGER.warn("Failed to reflect action performed", e);
        }
    }

    @Override
    public void openWarnToast(int failCount) {
        Minecraft.getMinecraft().getToastGui().add(new DataFailToast(failCount));
    }

    @Override
    public void scheduleTask(MessageContext ctx, Runnable task) {
        if (ctx.side.isClient()) {
            Minecraft.getMinecraft().addScheduledTask(task);
        } else {
            super.scheduleTask(ctx, task);
        }
    }

    @Override
    public World getWorld(MessageContext ctx) {
        if (ctx.side.isClient()) {
            return Minecraft.getMinecraft().world;
        } else {
            return super.getWorld(ctx);
        }
    }

    public static void actionPerformed(GuiScreen gui, GuiButton button) {
        if (actionPerformed != null) {
            try {
                actionPerformed.invoke(gui, button);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Terrarium.LOGGER.warn("Failed to invoke actionPerformed", e);
            }
        }
    }

    @Nullable
    public static GuiButton getStructuresButton(GuiCreateWorld gui) {
        if (structuresButton != null) {
            try {
                return (GuiButton) structuresButton.get(gui);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    public static int getSelectedWorldType(GuiCreateWorld gui) {
        if (selectedWorldType != null) {
            try {
                return (int) selectedWorldType.get(gui);
            } catch (IllegalAccessException e) {
                return 0;
            }
        }
        return 0;
    }
}
