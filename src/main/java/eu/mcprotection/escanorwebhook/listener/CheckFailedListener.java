package eu.mcprotection.escanorwebhook.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.discord.FailedWebhook;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;
import xyz.yooniks.escanorproxy.event.CheckFailedEvent;

@Singleton
public class CheckFailedListener implements Listener {

  @Inject private FailedWebhook failedWebhook;
  @Inject private EscanorProxyStatistics statistics;

  @EventHandler
  public void onCheckFailed(final CheckFailedEvent event) {
    this.failedWebhook.send(
        event.getPlayerName(),
        event.getHostAddress(),
        event.getFailed(),
        this.statistics.getConnectionsPerSecond());
  }
}
