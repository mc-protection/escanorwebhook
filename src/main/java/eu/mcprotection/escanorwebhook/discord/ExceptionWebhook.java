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

public class ExceptionWebhook {
  private WebhookClient client;

  public void connect() {
    final WebhookClientBuilder builder = new WebhookClientBuilder(ConfigUtil.getString("exception.url"));
    builder.setThreadFactory(job -> {
      final Thread thread = new Thread(job);
      thread.setName("Exception-Webhook-Thread");
      thread.setDaemon(true);
      return thread;
    });
    builder.setWait(true);

    this.client = builder.build();
    EscanorWebhook.PLUGIN.getPlugin().getLogger().info("Connected to exception webhook");
  }

  public void send(@NotNull final String plugin, @NotNull final String stacktrace) {
    EscanorWebhook.PLUGIN.getService().submit(() -> {
      final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
      builder.setTitle(new WebhookEmbed.EmbedTitle(ConfigUtil.getString("exception.embed.title"), ConfigUtil.getString("exception.embed.url")));
      builder.setColor(ConfigUtil.getInteger("exception.embed.color"));
      builder.setDescription(ConfigUtil.getString("exception.embed.description")
          .replace("{NL}", "\n")
          .replace("{0}", plugin)
          .replace("{1}", stacktrace.length() > 4096 ? stacktrace.substring(0, 4096) : stacktrace)
      );
      if (ConfigUtil.getBoolean("exception.embed.timestamp")) {
        builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
      }
      builder.setFooter(new WebhookEmbed.EmbedFooter(ConfigUtil.getString("exception.embed.footer.text"), ConfigUtil.getString("exception.embed.footer.icon_url")));

      try {
        this.client.send(builder.build());
      } catch (HttpException exception) {
        EscanorWebhook.PLUGIN.getProxyServer().getLogger().warning("Failed to send webhook: " + exception.getMessage());
      }
    });
  }
}
