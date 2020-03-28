package net.gegy1000.earth.server.command;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DeferredTranslator {
    private static final LanguageCache LANGUAGE_CACHE = new LanguageCache();
    private static Field languageField;

    static {
        try {
            languageField = ObfuscationReflectionHelper.findField(EntityPlayerMP.class, "field_71148_cg");
            languageField.setAccessible(true);
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            TerrariumEarth.LOGGER.warn("Failed to find language field", e);
        }
    }

    private static String getLanguageKey(ICommandSender sender) {
        if (languageField != null && sender instanceof EntityPlayerMP) {
            try {
                return (String) languageField.get(sender);
            } catch (ReflectiveOperationException e) {
                TerrariumEarth.LOGGER.warn("Failed to get language for player", e);
            }
        }
        return "en_US";
    }

    private static Language getLanguage(ICommandSender sender) {
        String languageKey = getLanguageKey(sender);
        return LANGUAGE_CACHE.getLanguage(languageKey);
    }

    public static ITextComponent translate(ICommandSender sender, ITextComponent component) {
        if (component instanceof TextComponentTranslation && sender instanceof EntityPlayer) {
            TextComponentTranslation translation = (TextComponentTranslation) component;
            if (!TerrariumUserTracker.usesTerrarium((EntityPlayer) sender)) {
                String key = translation.getKey();
                String translatedString = translateString(sender, key);
                return new TextComponentString(String.format(translatedString, translation.getFormatArgs()));
            }
        }
        return component;
    }

    public static WrongUsageException createException(ICommandSender sender, String key, Object... objects) {
        if (sender instanceof EntityPlayer && TerrariumUserTracker.usesTerrarium((EntityPlayer) sender)) {
            return new WrongUsageException(key, objects);
        }
        String translation = translateString(sender, key);
        return new WrongUsageException(translation, objects);
    }

    public static String translateStringOrKey(ICommandSender sender, String translationKey) {
        if (sender instanceof EntityPlayer && TerrariumUserTracker.usesTerrarium((EntityPlayer) sender)) {
            return translationKey;
        }
        return translateString(sender, translationKey);
    }

    public static String translateString(ICommandSender sender, String translationKey) {
        Language language = getLanguage(sender);
        String translation = language.get(translationKey);

        if (translation == null) {
            Language fallbackLanguage = LANGUAGE_CACHE.getFallbackLanguage();
            String fallback = fallbackLanguage.get(translationKey);
            return fallback != null ? fallback : translationKey;
        }

        return translation;
    }

    private static class LanguageCache {
        private final LoadingCache<String, Language> languages = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Language>() {
                    @Override
                    public Language load(String key) throws Exception {
                        return Language.open(key);
                    }
                });

        public Language getLanguage(String key) {
            try {
                return this.languages.get(key);
            } catch (ExecutionException e) {
                TerrariumEarth.LOGGER.warn("Failed to load language {}", key, e);
                return Language.EMPTY;
            }
        }

        public Language getFallbackLanguage() {
            return this.getLanguage("en_US");
        }
    }

    private static class Language {
        static final Language EMPTY = new Language(ImmutableMap.of());

        private final Map<String, String> languageMap;

        private Language(Map<String, String> languageMap) {
            this.languageMap = languageMap;
        }

        public static Language open(String name) throws IOException {
            try (InputStream input = DeferredTranslator.class.getResourceAsStream("/assets/earth/lang/" + name + ".lang")) {
                return new Language(LanguageMap.parseLangFile(input));
            }
        }

        @Nullable
        public String get(String key) {
            return this.languageMap.get(key);
        }
    }
}
