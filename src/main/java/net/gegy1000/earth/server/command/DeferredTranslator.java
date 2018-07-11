package net.gegy1000.earth.server.command;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.LanguageMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DeferredTranslator {
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();

    static {
        try (InputStream input = DeferredTranslator.class.getResourceAsStream("/assets/earth/lang/en_US.lang")) {
            LANGUAGE_MAP.putAll(LanguageMap.parseLangFile(input));
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to parse language file", e);
        }
    }

    public static ITextComponent translate(ICommandSender sender, ITextComponent component) {
        if (component instanceof TextComponentTranslation && sender instanceof EntityPlayer) {
            TextComponentTranslation translation = (TextComponentTranslation) component;
            if (!TerrariumHandshakeTracker.isFriendly((EntityPlayer) sender)) {
                String key = translation.getKey();
                return new TextComponentString(String.format(LANGUAGE_MAP.getOrDefault(key, key), translation.getFormatArgs()));
            }
        }
        return component;
    }

    public static WrongUsageException createException(ICommandSender sender, String key, Object... objects) {
        if (sender instanceof EntityPlayer && TerrariumHandshakeTracker.isFriendly((EntityPlayer) sender)) {
            return new WrongUsageException(key, objects);
        }
        return new WrongUsageException(LANGUAGE_MAP.getOrDefault(key, key), objects);
    }

    public static String translateString(ICommandSender sender, String translationKey) {
        if (sender instanceof EntityPlayer && TerrariumHandshakeTracker.isFriendly((EntityPlayer) sender)) {
            return translationKey;
        }
        return LANGUAGE_MAP.getOrDefault(translationKey, translationKey);
    }
}
