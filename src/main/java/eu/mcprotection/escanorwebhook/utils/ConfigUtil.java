package eu.mcprotection.escanorwebhook.utils;

import eu.mcprotection.escanorwebhook.EscanorWebhook;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ConfigUtil {
  public static String getString(@NotNull final String path) {
    return EscanorWebhook.PLUGIN.getConfiguration().getString(path);
  }

  public static int getInteger(@NotNull final String path) {
    return EscanorWebhook.PLUGIN.getConfiguration().getInt(path);
  }

  public static boolean getBoolean(@NotNull final String path) {
    return EscanorWebhook.PLUGIN.getConfiguration().getBoolean(path);
  }

  public static boolean isShow(@NotNull final String path, @NotNull final String fieldName) {
    return getBoolean(path + ".embed.fields." + fieldName + ".show");
  }

  public static boolean isInline(@NotNull final String path, @NotNull final String fieldName) {
    return getBoolean(path + ".embed.fields." + fieldName + ".inline");
  }

  public static String getName(@NotNull final String path, @NotNull final String fieldName) {
    return getString(path + ".embed.fields." + fieldName + ".name");
  }

  public static String getValue(@NotNull final String path, @NotNull final String fieldName) {
    return getString(path + ".embed.fields." + fieldName + ".value");
  }
}
