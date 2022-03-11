package eu.mcprotection.escanorwebhook.config;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;

@Singleton
public final class ResourceLoader {

  @Inject private Plugin plugin;
  @Inject private ResourceRepository resourceRepository;

  public void loadResources() {
    Configuration configuration = this.loadResource("config.yml");
    if (configuration != null) {
      this.resourceRepository.add(ResourceRepository.MAIN_CONFIG_PATH, configuration);
    }
  }

  public Configuration loadResource(@NotNull final String resource) {
    final File folder = this.plugin.getDataFolder();
    if (!folder.exists()) {
      folder.mkdir();
    }

    final File resourceFile = new File(folder, resource);
    try {
      if (!resourceFile.exists()) {
        resourceFile.createNewFile();
        try (InputStream in = this.plugin.getResourceAsStream(resource);
            OutputStream out = new FileOutputStream(resourceFile)) {
          ByteStreams.copy(in, out);
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }

    try {
      return ConfigurationProvider.getProvider(YamlConfiguration.class).load(resourceFile);
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }
}
