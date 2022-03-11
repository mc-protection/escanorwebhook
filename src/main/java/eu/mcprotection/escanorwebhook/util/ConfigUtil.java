package eu.mcprotection.escanorwebhook.util;

import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

public class ConfigUtil {

  public static boolean isShow(
      @NotNull Configuration configuration,
      @NotNull final String path,
      @NotNull final String fieldName) {
    return configuration.getBoolean(path + ".embed.fields." + fieldName + ".show");
  }

  public static boolean isInline(
      Configuration configuration, @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getBoolean(path + ".embed.fields." + fieldName + ".inline");
  }

  public static String getName(
      Configuration configuration, @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getString(path + ".embed.fields." + fieldName + ".name");
  }

  public static String getValue(
      Configuration configuration, @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getString(path + ".embed.fields." + fieldName + ".value");
  }
}
