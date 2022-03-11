package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import eu.mcprotection.escanorwebhook.util.ConfigUtil;
import eu.mcprotection.escanorwebhook.util.ServerResourcesUtils;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;
import xyz.yooniks.escanorproxy.EscanorUtil;

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
    this.scheduledService.scheduleAtFixedRate(() -> {
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
        this.getConfig().getInt("attack.extra.scheduler_delay"),
        this.getConfig().getInt("attack.extra.scheduler_delay"),
        TimeUnit.SECONDS);
  }

  private boolean attackDetected(final boolean start) {
    final int mode =
        start
            ? this.getConfig().getInt("attack.extra.mode.start")
            : this.getConfig().getInt("attack.extra.mode.end");
    switch (mode) {
      case 1:
        return start
            ? this.getCps() >= this.getConfig().getInt("attack.extra.cps.start")
            : this.getCps() <= this.getConfig().getInt("attack.extra.cps.end");
      case 2:
        return start == EscanorUtil.underAttack;
      case 3:
        return start
            ? this.getCps() >= this.getConfig().getInt("attack.extra.cps.start")
            && EscanorUtil.underAttack
            : this.getCps() <= this.getConfig().getInt("attack.extra.cps.end")
                && !EscanorUtil.underAttack;
      default:
        throw new IllegalStateException("Unknown attack mode: " + mode);
    }
  }

  private void sendByType(@NotNull final SendType type) {
    final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
    builder.setTitle(new WebhookEmbed.EmbedTitle(
        this.underAttack
            ? this.getConfig().getString("attack.embed.title.start")
            : this.getConfig().getString("attack.embed.title.end"),
        this.getConfig().getString("attack.embed.url")));
    builder.setColor(this.getConfig().getInt("attack.embed.color"));
    builder.setDescription(
        this.underAttack
            ? this.getConfig().getString("attack.embed.description.start")
            : this.getConfig().getString("attack.embed.description.end"));
    if (ConfigUtil.isShow(this.getConfig(), "attack", "cps")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "cps"),
              ConfigUtil.getName(this.getConfig(), "attack", "cps"),
              ConfigUtil.getValue(this.getConfig(), "attack", "cps")
                  .replace("{0}", String.valueOf(this.getHighestCps()))
                  .replace("{1}", String.valueOf(this.getCps()))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "pps")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "pps"),
              ConfigUtil.getName(this.getConfig(), "attack", "pps"),
              ConfigUtil.getValue(this.getConfig(), "attack", "pps")
                  .replace("{0}", String.valueOf(this.statistics.getPingsPerSecond()))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "blocked")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "blocked"),
              ConfigUtil.getName(this.getConfig(), "attack", "blocked"),
              ConfigUtil.getValue(this.getConfig(), "attack", "blocked")
                  .replace("{0}", String.valueOf(this.statistics.getBlockedConnections()))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "blacklisted")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "blacklisted"),
              ConfigUtil.getName(this.getConfig(), "attack", "blacklisted"),
              ConfigUtil.getValue(this.getConfig(), "attack", "blacklisted")
                  .replace("{0}", String.valueOf(this.statistics.getBlacklistedConnections()))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "attack")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "attack"),
              ConfigUtil.getName(this.getConfig(), "attack", "attack"),
              ConfigUtil.getValue(this.getConfig(), "attack", "attack")
                  .replace("{0}", String.valueOf(EscanorUtil.underAttack))
                  .replace("{1}", String.valueOf(EscanorUtil.botCounter))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "cpu")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "cpu"),
              ConfigUtil.getName(this.getConfig(), "attack", "cpu"),
              ConfigUtil.getValue(this.getConfig(), "attack", "cpu")
                  .replace("{0}", String.valueOf(ServerResourcesUtils.getProcessCpuLoad()))));
    }

    if (ConfigUtil.isShow(this.getConfig(), "attack", "ram")) {
      builder.addField(
          new WebhookEmbed.EmbedField(
              ConfigUtil.isInline(this.getConfig(), "attack", "ram"),
              ConfigUtil.getName(this.getConfig(), "attack", "ram"),
              ConfigUtil.getValue(this.getConfig(), "attack", "ram")
                  .replace("{0}", String.valueOf(ServerResourcesUtils.getMemory()))));
    }
    if (this.getConfig().getBoolean("attack.embed.timestamp")) {
      builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
    }
    builder.setFooter(
        new WebhookEmbed.EmbedFooter(
            this.getConfig().getString("attack.embed.footer.text"),
            this.getConfig().getString("attack.embed.footer.icon_url")));

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
    if (this.getConfig().getBoolean("attack.extra.check_total_cps")) {
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
