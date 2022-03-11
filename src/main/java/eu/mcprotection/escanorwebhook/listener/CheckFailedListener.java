package eu.mcprotection.escanorwebhook.listener;

import eu.mcprotection.escanorwebhook.EscanorWebhook;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.yooniks.escanorproxy.event.CheckFailedEvent;

public class CheckFailedListener implements Listener {
  @EventHandler
  public void onCheckFailed(final CheckFailedEvent event) {
    EscanorWebhook.PLUGIN.getFailedWebhook().send(event.getPlayerName(), event.getHostAddress(), event.getFailed(), EscanorWebhook.PLUGIN.getStatistics().getConnectionsPerSecond());
  }
}
