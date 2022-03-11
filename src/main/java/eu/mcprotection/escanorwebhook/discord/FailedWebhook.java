package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import eu.mcprotection.escanorwebhook.util.ConfigUtil;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class FailedWebhook extends Webhook {

  @Inject private Plugin plugin;
  @Inject private ExecutorService service;
  @Inject private ScheduledExecutorService scheduledService;

  private int blockedConnections;

  @Inject
  private FailedWebhook(ResourceRepository resourceRepository) {
    super(resourceRepository);
  }

  public void connect() {
    super.connect("failed.url", "Failed-Webhook-Thread");
    this.plugin.getLogger().info("Connected to failed webhook");
  }

  public void send(
      @NotNull final String playerName,
      @NotNull final String hostAddress,
      @NotNull final String failed,
      final int cps) {
    if (cps >= this.config.getInt("failed.extra.cancel_cps")) {
      System.out.println("Cancelling failed webhook due to high CPS");
      return;
    }

    this.blockedConnections++;
    if (cps > this.config.getInt("failed.extra.cancel_cps")
        && this.blockedConnections > this.config.getInt("failed.extra.blocked_connections")) {
      System.out.println("Skipping failed webhook due to blocked connections");
      return;
    }

    this.getService()
        .submit(
            () -> {
              final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
              builder.setTitle(
                  new WebhookEmbed.EmbedTitle(
                      this.config.getString("failed.embed.title"),
                      this.config.getString("failed.embed.url")));
              builder.setColor(this.config.getInt("failed.embed.color"));
              builder.setDescription(this.config.getString("failed.embed.description"));

              if (ConfigUtil.isShow(this.config, "failed", "player_name")) {
                builder.addField(
                    new WebhookEmbed.EmbedField(
                        ConfigUtil.isInline(this.config, "failed", "player_name"),
                        ConfigUtil.getName(this.config, "failed", "player_name"),
                        ConfigUtil.getValue(this.config, "failed", "player_name")
                            .replace("{0}", playerName)));
              }

              if (ConfigUtil.isShow(this.config, "failed", "player_address")) {
                builder.addField(
                    new WebhookEmbed.EmbedField(
                        ConfigUtil.isInline(this.config, "failed", "player_address"),
                        ConfigUtil.getName(this.config, "failed", "player_address"),
                        ConfigUtil.getValue(this.config, "failed", "player_address")
                            .replace("{0}", hostAddress)));
              }

              if (ConfigUtil.isShow(this.config, "failed", "player_failed")) {
                builder.addField(
                    new WebhookEmbed.EmbedField(
                        ConfigUtil.isInline(this.config, "failed", "player_failed"),
                        ConfigUtil.getName(this.config, "failed", "player_failed"),
                        ConfigUtil.getValue(this.config, "failed", "player_failed")
                            .replace("{0}", failed)));
              }
              if (this.config.getBoolean("failed.embed.timestamp")) {
                builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
              }

              builder.setFooter(
                  new WebhookEmbed.EmbedFooter(
                      this.config.getString("failed.embed.footer.text"),
                      this.config.getString("failed.embed.footer.icon_url")));

              try {
                this.client.send(builder.build());
              } catch (HttpException exception) {
                this.plugin
                    .getLogger()
                    .warning("Failed to send webhook: " + exception.getMessage());
              }
            });

    this.getScheduledService()
        .scheduleAtFixedRate(
            () -> {
              this.blockedConnections = 0;
            },
            this.config.getInt("failed.extra.scheduler_delay"),
            this.config.getInt("failed.extra.scheduler_delay"),
            TimeUnit.SECONDS);
  }
}
