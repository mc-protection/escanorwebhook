package eu.mcprotection.escanorwebhook.repository;

import com.google.inject.Singleton;
import net.md_5.bungee.config.Configuration;

import java.util.HashMap;
import java.util.Map;

@Singleton
public final class ResourceRepository {
  public static final String MAIN_CONFIG_PATH = "main-config";

  private Map<String, Configuration> configurations = new HashMap<>();

  public void add(String key, Configuration configuration) {
    this.configurations.put(key, configuration);
  }

  public Configuration get(String key) {
    return this.configurations.get(key);
  }
}
