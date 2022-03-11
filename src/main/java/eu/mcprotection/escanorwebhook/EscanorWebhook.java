package eu.mcprotection.escanorwebhook;

import com.google.common.io.ByteStreams;
import eu.mcprotection.escanorwebhook.discord.AttackWebhook;
import eu.mcprotection.escanorwebhook.discord.ExceptionWebhook;
import eu.mcprotection.escanorwebhook.discord.FailedWebhook;
import eu.mcprotection.escanorwebhook.listener.CheckFailedListener;
import eu.mcprotection.escanorwebhook.listener.ProxyExceptionListener;
import eu.mcprotection.escanorwebhook.utils.ConfigUtil;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public enum EscanorWebhook {
  PLUGIN;

  @Getter
  private EscanorWebhookPlugin plugin;

  @Getter
  private Configuration configuration;
  @Getter
  EscanorProxyStatistics statistics;
  @Getter
  private ProxyServer proxyServer;
  private PluginManager pluginManager;

  @Getter
  private ExecutorService service;
  @Getter
  private ScheduledExecutorService scheduledService;

  @Getter
  private FailedWebhook failedWebhook;
  @Getter
  private ExceptionWebhook exceptionWebhook;
  @Getter
  private AttackWebhook attackWebhook;

  /**
   * Set the plugin instance.
   *
   * @param plugin the plugin - {@link EscanorWebhookPlugin} - instance
   */
  public void load(@NotNull final EscanorWebhookPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Register all commands and listeners when the plugin is enabled.
   */
  public void start() {
    this.init();
    this.proxyServer.getLogger().info("EscanorWebhook plugin has been started!");
  }

  /**
   * Send a message to the console when the plugin is stopped.
   */
  public void stop() {
    this.proxyServer.getLogger().info("EscanorWebhook plugin has been stopped!");
  }

  /**
   * Initialize everything.
   */
  private void init() {
    try {
      this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.loadResource("config.yml"));
    } catch (IOException exception) {
      System.out.println("[EscanorWebhook]: Could not load configuration file!");
      throw new RuntimeException(exception);
    }
    this.statistics = new EscanorProxyStatistics();
    this.proxyServer = this.plugin.getProxy();
    this.pluginManager = this.proxyServer.getPluginManager();
    this.registerListener();

    this.service = Executors.newFixedThreadPool(3);
    this.scheduledService = Executors.newScheduledThreadPool(2);

    this.attackWebhook = new AttackWebhook();
    if (ConfigUtil.getBoolean("attack.enable")) {
      this.attackWebhook.connect();
      this.attackWebhook.send();
    }

    this.exceptionWebhook = new ExceptionWebhook();
    if (ConfigUtil.getBoolean("exception.enable")) {
      this.exceptionWebhook.connect();
    }
    
    this.failedWebhook = new FailedWebhook();
    if (ConfigUtil.getBoolean("failed.enable")) {
      this.failedWebhook.connect();
    }
  }

  /**
   * Register all listeners.
   */
  private void registerListener() {
    if (ConfigUtil.getBoolean("failed.enable")) {
      this.pluginManager.registerListener(this.plugin, new CheckFailedListener());
    }

    if (ConfigUtil.getBoolean("exception.enable")) {
      this.pluginManager.registerListener(this.plugin, new ProxyExceptionListener());
    }
  }

  private File loadResource(@NotNull final String resource) {
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
      this.plugin.getProxy().getLogger().warning("Could not load resource file: " + resource);
      throw new RuntimeException(exception);
    }
    return resourceFile;
  }
}
