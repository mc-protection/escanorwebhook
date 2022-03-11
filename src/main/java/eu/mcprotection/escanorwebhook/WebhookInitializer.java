package eu.mcprotection.escanorwebhook;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.config.ResourceLoader;
import eu.mcprotection.escanorwebhook.discord.AttackWebhook;
import eu.mcprotection.escanorwebhook.discord.ExceptionWebhook;
import eu.mcprotection.escanorwebhook.discord.FailedWebhook;
import eu.mcprotection.escanorwebhook.listener.CheckFailedListener;
import eu.mcprotection.escanorwebhook.listener.ProxyExceptionListener;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class WebhookInitializer {

  @Inject
  private Injector injector;
  @Inject
  private Plugin plugin;
  @Inject
  private ResourceLoader resourceLoader;
  @Inject
  private ResourceRepository resourceRepository;
  @Inject
  private AttackWebhook attackWebhook;
  @Inject
  private ExceptionWebhook exceptionWebhook;
  @Inject
  private FailedWebhook failedWebhook;

  public void initialize() {
    // Load Resources
    this.resourceLoader.loadResources();

    // Init Webhooks
    this.initWebhooks();

    // Register Listener
    this.registerListener();
  }

  private void initWebhooks() {
    if (this.getConfig().getBoolean("attack.enable")) {
      this.attackWebhook.connect();
      this.attackWebhook.send();
    }

    if (this.getConfig().getBoolean("exception.enable")) {
      this.exceptionWebhook.connect();
    }

    if (this.getConfig().getBoolean("failed.enable")) {
      this.failedWebhook.connect();
    }
  }

  private void registerListener() {
    final PluginManager pluginManager = this.plugin.getProxy().getPluginManager();
    if (this.getConfig().getBoolean("failed.enable")) {
      pluginManager.registerListener(this.plugin, this.getInstance(CheckFailedListener.class));
    }

    if (this.getConfig().getBoolean("exception.enable")) {
      pluginManager.registerListener(this.plugin, this.getInstance(ProxyExceptionListener.class));
    }
  }

  private <T> T getInstance(@NotNull final Class<T> clazz) {
    return this.injector.getInstance(clazz);
  }

  private Configuration getConfig() {
    return this.resourceRepository.get(ResourceRepository.MAIN_CONFIG_PATH);
  }
}
