package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import eu.mcprotection.escanorwebhook.util.ConfigUtil;
import eu.mcprotection.escanorwebhook.util.ServerResourcesUtils;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;
import xyz.yooniks.escanorproxy.EscanorUtil;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class AttackWebhook extends Webhook {

  @Inject private Plugin plugin;
  @Inject private EscanorProxyStatistics statistics;
  @Inject private ScheduledExecutorService scheduledService;

  private boolean underAttack = false;
  private long messageId = -1;
  private int beforeCps = 0;

  @Inject
  private AttackWebhook(ResourceRepository resourceRepository) {
    super(resourceRepository);
  }

  public void connect() {
    super.connect("attack.url", "Attack-Webhook-Thread");
    this.plugin.getLogger().info("Connected to attack webhook");
  }

  public void send() {
    this.scheduledService.scheduleAtFixedRate(
        () -> {
          if (!this.underAttack) {
            if (this.attackDetected(true)) {
              this.underAttack = true;
              this.sendByType(SendType.SEND);
            }
          }

          if (this.underAttack) {
            if (this.attackDetected(false)) {
              this.beforeCps = 0;
              this.underAttack = false;
              this.sendByType(SendType.SEND_AND_SET);
              return;
            }

            if (this.messageId == -1) {
              return;
            }

            this.sendByType(SendType.EDIT);
          }
        },
        this.config.getInt("attack.extra.scheduler_delay"),
        this.config.getInt("attack.extra.scheduler_delay"),
        TimeUnit.SECONDS);
  }

  private boolean attackDetected(final boolean start) {
    final int mode =
        start
            ? this.config.getInt("attack.extra.mode.start")
            : this.config.getInt("attack.extra.mode.end");
    switch (mode) {
      case 1:
        return start
            ? this.getCps() >= this.config.getInt("attack.extra.cps.start")
            : this.getCps() <= this.config.getInt("attack.extra.cps.end");
      case 2:
        return start == EscanorUtil.underAttack;
      case 3:
        return start
            ? this.getCps() >= this.config.getInt("attack.extra.cps.start")
                && EscanorUtil.underAttack
            : this.getCps() <= this.config.getInt("attack.extra.cps.end")
                && !EscanorUtil.underAttack;
      default:
        throw new IllegalStateException("Unknown attack mode: " + mode);
    }
  }

  private void sendByType(@NotNull final SendType type) {
    final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
    builder.setTitle(
        new WebhookEmbed.EmbedTitle(
            this.underAttack
                ? this.config.getString("attack.embed.title.start")
                : this.config.getString("attack.embed.title.end"),
            this.config.getString("attack.embed.url")));
    builder.setColor(this.config.getInt("attack.embed.color"));
    builder.setDescription(
        this.underAttack
            ? this.config.getString("attack.embed.description.start")
            : this.config.getString("attack.embed.description.end"));
    if (ConfigUtil.isShow(this.config, "attack", "cps")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "cps"),
              ConfigUtil.getName(this.config, "attack", "cps"),
              ConfigUtil.getValue(this.config, "attack", "cps")
                  .replace("{0}", String.valueOf(this.getHighestCps()))
                  .replace("{1}", String.valueOf(this.getCps()))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "pps")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "pps"),
              ConfigUtil.getName(this.config, "attack", "pps"),
              ConfigUtil.getValue(this.config, "attack", "pps")
                  .replace("{0}", String.valueOf(this.statistics.getPingsPerSecond()))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "blocked")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "blocked"),
              ConfigUtil.getName(this.config, "attack", "blocked"),
              ConfigUtil.getValue(this.config, "attack", "blocked")
                  .replace("{0}", String.valueOf(this.statistics.getBlockedConnections()))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "blacklisted")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "blacklisted"),
              ConfigUtil.getName(this.config, "attack", "blacklisted"),
              ConfigUtil.getValue(this.config, "attack", "blacklisted")
                  .replace("{0}", String.valueOf(this.statistics.getBlacklistedConnections()))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "attack")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "attack"),
              ConfigUtil.getName(this.config, "attack", "attack"),
              ConfigUtil.getValue(this.config, "attack", "attack")
                  .replace("{0}", String.valueOf(EscanorUtil.underAttack))
                  .replace("{1}", String.valueOf(EscanorUtil.botCounter))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "cpu")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "cpu"),
              ConfigUtil.getName(this.config, "attack", "cpu"),
              ConfigUtil.getValue(this.config, "attack", "cpu")
                  .replace("{0}", String.valueOf(ServerResourcesUtils.getProcessCpuLoad()))));
    }

    if (ConfigUtil.isShow(this.config, "attack", "ram")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.config, "attack", "ram"),
              ConfigUtil.getName(this.config, "attack", "ram"),
              ConfigUtil.getValue(this.config, "attack", "ram")
                  .replace("{0}", String.valueOf(ServerResourcesUtils.getMemory()))));
    }
    if (this.config.getBoolean("attack.embed.timestamp")) {
      builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
    }
    builder.setFooter(
        new WebhookEmbed.EmbedFooter(
            this.config.getString("attack.embed.footer.text"),
            this.config.getString("attack.embed.footer.icon_url")));

    switch (type) {
      case SEND:
        try {
          this.client
              .send(builder.build())
              .thenAccept(readonlyMessage -> this.messageId = readonlyMessage.getId());
        } catch (HttpException exception) {
          this.plugin.getLogger().warning("Failed to send webhook: " + exception.getMessage());
        }
        break;

      case SEND_AND_SET:
        try {
          this.client.send(builder.build()).thenRun(() -> this.messageId = -1L);
        } catch (HttpException exception) {
          this.plugin.getLogger().warning("Failed to send webhook: " + exception.getMessage());
        }
        break;

      case EDIT:
        try {
          this.client.edit(this.messageId, builder.build());
        } catch (HttpException exception) {
          this.plugin.getLogger().warning("Failed to send webhook: " + exception.getMessage());
        }
        break;
    }
  }

  private int getHighestCps() {
    if (this.getCps() > this.beforeCps) {
      this.beforeCps = this.getCps();
      return beforeCps;
    }
    return this.beforeCps;
  }

  private int getCps() {
    if (this.config.getBoolean("attack.extra.check_total_cps")) {
      return this.statistics.getTotalConnectionsPerSecond();
    } else {
      return this.statistics.getConnectionsPerSecond();
    }
  }

  private enum SendType {
    SEND,
    SEND_AND_SET,
    EDIT
  }
}
