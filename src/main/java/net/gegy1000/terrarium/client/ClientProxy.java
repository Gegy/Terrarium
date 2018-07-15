package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.toast.DataFailToast;
import net.gegy1000.terrarium.server.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    private static Method actionPerformed;
    private static Field selectedWorldType;

    @Override
    public void onPreInit() {
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onPostInit() {
        try {
            ClientProxy.selectedWorldType = reflectSelectedWorldType();
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.warn("Failed to reflect selected world type", e);
        }

        try {
            ClientProxy.actionPerformed = reflectActionPerformed();
        } catch (ReflectiveOperationException e) {
            Terrarium.LOGGER.warn("Failed to reflect action performed", e);
        }
    }

    @Override
    public boolean hasServer() {
        return Minecraft.getMinecraft().isIntegratedServerRunning();
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

    private static Field reflectSelectedWorldType() throws ReflectiveOperationException {
        for (Field field : GuiCreateWorld.class.getDeclaredFields()) {
            if (field.getType() == int.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new ReflectiveOperationException("Could not find selected world type field");
    }

    private static Method reflectActionPerformed() throws ReflectiveOperationException {
        for (Method method : GuiScreen.class.getDeclaredMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == GuiButton.class && method.getReturnType() == void.class) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new ReflectiveOperationException("Could not find action performed method");
    }

    public static void actionPerformed(GuiScreen gui, GuiButton button) {
        if (ClientProxy.actionPerformed != null) {
            try {
                ClientProxy.actionPerformed.invoke(gui, button);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Terrarium.LOGGER.warn("Failed to invoke actionPerformed", e);
            }
        }
    }

    public static int getSelectedWorldType(GuiCreateWorld gui) {
        if (ClientProxy.selectedWorldType == null) {
            return 0;
        }
        try {
            return (int) ClientProxy.selectedWorldType.get(gui);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }
}
