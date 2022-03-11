package eu.mcprotection.escanorwebhook;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;

@Singleton
public class EscanorWebhookPlugin extends Plugin {

  @Override
  public void onEnable() {
    final Injector injector = Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Plugin.class).toInstance(EscanorWebhookPlugin.this);
            bind(EscanorProxyStatistics.class).toInstance(new EscanorProxyStatistics());

            bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(3));
            bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(2));
          }
        });

    injector.getInstance(WebhookInitializer.class).initialize();
  }
}
