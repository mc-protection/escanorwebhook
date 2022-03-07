package eu.mcprotection.escanorbot.listener;

import eu.mcprotection.escanorbot.EscanorBot;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.yooniks.escanorproxy.event.CheckFailedEvent;

public class CheckFailedListener implements Listener {
  @EventHandler
  public void onCheckFailed(final CheckFailedEvent event) {
    EscanorBot.PLUGIN.getFailedWebhook().send(event.getPlayerName(), event.getHostAddress(), event.getFailed(), EscanorBot.PLUGIN.getStatistics().getConnectionsPerSecond());
  }
}
