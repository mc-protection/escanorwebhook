package eu.mcprotection.escanorwebhook.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.discord.ExceptionWebhook;
import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Singleton
public class ProxyExceptionListener implements Listener {

  @Inject private ExceptionWebhook exceptionWebhook;

  @EventHandler
  public void onProxyException(final ProxyExceptionEvent event) {
    this.exceptionWebhook.send(
        ExceptionUtils.getRootCauseMessage(event.getException().getCause()),
        ExceptionUtils.getStackTrace(event.getException()));
  }
}
