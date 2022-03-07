package eu.mcprotection.escanorbot.listener;

import eu.mcprotection.escanorbot.EscanorBot;
import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ProxyExceptionListener implements Listener {
  @EventHandler
  public void onProxyException(final ProxyExceptionEvent event) {
    EscanorBot.PLUGIN.getExceptionWebhook().send(ExceptionUtils.getRootCauseMessage(event.getException().getCause()), ExceptionUtils.getStackTrace(event.getException()));
  }
}
