package eu.mcprotection.escanorwebhook.listener;

import eu.mcprotection.escanorwebhook.EscanorWebhook;
import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ProxyExceptionListener implements Listener {
  @EventHandler
  public void onProxyException(final ProxyExceptionEvent event) {
    EscanorWebhook.PLUGIN.getExceptionWebhook().send(ExceptionUtils.getRootCauseMessage(event.getException().getCause()), ExceptionUtils.getStackTrace(event.getException()));
  }
}
