package eu.mcprotection.escanorwebhook.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ConfigUtil {

  public static boolean isShow(@NotNull final Configuration configuration,
      @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getBoolean(path + ".embed.fields." + fieldName + ".show");
  }

  public static boolean isInline(@NotNull final Configuration configuration,
      @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getBoolean(path + ".embed.fields." + fieldName + ".inline");
  }

  public static String getName(@NotNull final Configuration configuration,
      @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getString(path + ".embed.fields." + fieldName + ".name");
  }

  public static String getValue(@NotNull final Configuration configuration,
      @NotNull final String path, @NotNull final String fieldName) {
    return configuration.getString(path + ".embed.fields." + fieldName + ".value");
  }
}
