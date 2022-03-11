package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import eu.mcprotection.escanorwebhook.EscanorWebhook;
import eu.mcprotection.escanorwebhook.utils.ConfigUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class FailedWebhook {
  private WebhookClient client;
  private int blockedConnections;

  public void connect() {
    final WebhookClientBuilder builder = new WebhookClientBuilder(ConfigUtil.getString("failed.url"));
    builder.setThreadFactory(job -> {
      final Thread thread = new Thread(job);
      thread.setName("Failed-Webhook-Thread");
      thread.setDaemon(true);
      return thread;
    });
    builder.setWait(true);

    this.client = builder.build();
    EscanorWebhook.PLUGIN.getPlugin().getLogger().info("Connected to failed webhook");
  }

  public void send(@NotNull final String playerName, @NotNull final String hostAddress, @NotNull final String failed, final int cps) {
    if (cps >= ConfigUtil.getInteger("failed.extra.cancel_cps")) {
      System.out.println("Cancelling failed webhook due to high CPS");
      return;
    }

    this.blockedConnections++;
    if (cps > ConfigUtil.getInteger("failed.extra.cancel_cps") && this.blockedConnections > ConfigUtil.getInteger("failed.extra.blocked_connections")) {
      System.out.println("Skipping failed webhook due to blocked connections");
      return;
    }

    EscanorWebhook.PLUGIN.getService().submit(() -> {
      final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
      builder.setTitle(new WebhookEmbed.EmbedTitle(ConfigUtil.getString("failed.embed.title"), ConfigUtil.getString("failed.embed.url")));
      builder.setColor(ConfigUtil.getInteger("failed.embed.color"));
      builder.setDescription(ConfigUtil.getString("failed.embed.description"));

      if (ConfigUtil.isShow("failed", "player_name")) {
        builder.addField(new WebhookEmbed.EmbedField(
            ConfigUtil.isInline("failed", "player_name"),
            ConfigUtil.getName("failed", "player_name"),
            ConfigUtil.getValue("failed", "player_name").replace("{0}", playerName)
        ));
      }

      if (ConfigUtil.isShow("failed", "player_address")) {
        builder.addField(new WebhookEmbed.EmbedField(
            ConfigUtil.isInline("failed", "player_address"),
            ConfigUtil.getName("failed", "player_address"),
            ConfigUtil.getValue("failed", "player_address").replace("{0}", hostAddress)
        ));
      }

      if (ConfigUtil.isShow("failed", "player_failed")) {
        builder.addField(new WebhookEmbed.EmbedField(
            ConfigUtil.isInline("failed", "player_failed"),
            ConfigUtil.getName("failed", "player_failed"),
            ConfigUtil.getValue("failed", "player_failed").replace("{0}", failed)
        ));
      }
      if (ConfigUtil.getBoolean("failed.embed.timestamp")) {
        builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
      }

      builder.setFooter(new WebhookEmbed.EmbedFooter(ConfigUtil.getString("failed.embed.footer.text"), ConfigUtil.getString("failed.embed.footer.icon_url")));

      try {
        this.client.send(builder.build());
      } catch (HttpException exception) {
        EscanorWebhook.PLUGIN.getProxyServer().getLogger().warning("Failed to send webhook: " + exception.getMessage());
      }
    });

    EscanorWebhook.PLUGIN.getScheduledService().scheduleAtFixedRate(() -> {
      this.blockedConnections = 0;
    }, ConfigUtil.getInteger("failed.extra.scheduler_delay"), ConfigUtil.getInteger("failed.extra.scheduler_delay"), TimeUnit.SECONDS);
  }
}
