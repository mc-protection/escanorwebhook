package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import eu.mcprotection.escanorwebhook.EscanorWebhook;
import eu.mcprotection.escanorwebhook.utils.ConfigUtil;
import eu.mcprotection.escanorwebhook.utils.ServerUtil;
import org.jetbrains.annotations.NotNull;
import xyz.yooniks.escanorproxy.EscanorProxyStatistics;
import xyz.yooniks.escanorproxy.EscanorUtil;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class AttackWebhook {
  private final EscanorProxyStatistics statistics;
  private boolean underAttack;
  private long messageId;
  private int beforeCps;

  private WebhookClient client;

  public AttackWebhook() {
    this.statistics = EscanorWebhook.PLUGIN.getStatistics();
    this.underAttack = false;
    this.messageId = -1;
    this.beforeCps = 0;
  }

  public void connect() {
    final WebhookClientBuilder builder = new WebhookClientBuilder(ConfigUtil.getString("attack.url"));
    builder.setThreadFactory(job -> {
      final Thread thread = new Thread(job);
      thread.setName("Attack-Webhook-Thread");
      thread.setDaemon(true);
      return thread;
    });
    builder.setWait(true);

    this.client = builder.build();
    EscanorWebhook.PLUGIN.getPlugin().getLogger().info("Connected to attack webhook");
  }

  public void send() {
    EscanorWebhook.PLUGIN.getScheduledService().scheduleAtFixedRate(() -> {
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
    }, ConfigUtil.getInteger("attack.extra.scheduler_delay"), ConfigUtil.getInteger("attack.extra.scheduler_delay"), TimeUnit.SECONDS);
  }

  private boolean attackDetected(final boolean start) {
    final int mode = start ? ConfigUtil.getInteger("attack.extra.mode.start") : ConfigUtil.getInteger("attack.extra.mode.end");
    switch (mode) {
      case 1:
        return start ? this.getCps() >= ConfigUtil.getInteger("attack.extra.cps.start") : this.getCps() <= ConfigUtil.getInteger("attack.extra.cps.end");
      case 2:
        return start == EscanorUtil.underAttack;
      case 3:
        return start ? this.getCps() >= ConfigUtil.getInteger("attack.extra.cps.start") && EscanorUtil.underAttack
            : this.getCps() <= ConfigUtil.getInteger("attack.extra.cps.end") && !EscanorUtil.underAttack;
      default:
        throw new IllegalStateException("Unknown attack mode: " + mode);
    }
  }

  private void sendByType(@NotNull final SendType type) {
    final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
    builder.setTitle(new WebhookEmbed.EmbedTitle(this.underAttack ? ConfigUtil.getString("attack.embed.title.start")
        : ConfigUtil.getString("attack.embed.title.end"), ConfigUtil.getString("attack.embed.url")));
    builder.setColor(ConfigUtil.getInteger("attack.embed.color"));
    builder.setDescription(this.underAttack ? ConfigUtil.getString("attack.embed.description.start") : ConfigUtil.getString("attack.embed.description.end"));
    if (ConfigUtil.isShow("attack", "cps")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "cps"),
          ConfigUtil.getName("attack", "cps"),
          ConfigUtil.getValue("attack", "cps")
              .replace("{0}", String.valueOf(this.getHighestCps()))
              .replace("{1}", String.valueOf(this.getCps()))
      ));
    }

    if (ConfigUtil.isShow("attack", "pps")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "pps"),
          ConfigUtil.getName("attack", "pps"),
          ConfigUtil.getValue("attack", "pps").replace("{0}", String.valueOf(this.statistics.getPingsPerSecond()))
      ));
    }

    if (ConfigUtil.isShow("attack", "blocked")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "blocked"),
          ConfigUtil.getName("attack", "blocked"),
          ConfigUtil.getValue("attack", "blocked").replace("{0}", String.valueOf(this.statistics.getBlockedConnections()))
      ));
    }

    if (ConfigUtil.isShow("attack", "blacklisted")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "blacklisted"),
          ConfigUtil.getName("attack", "blacklisted"),
          ConfigUtil.getValue("attack", "blacklisted").replace("{0}", String.valueOf(this.statistics.getBlacklistedConnections()))
      ));
    }

    if (ConfigUtil.isShow("attack", "attack")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "attack"),
          ConfigUtil.getName("attack", "attack"),
          ConfigUtil.getValue("attack", "attack")
              .replace("{0}", String.valueOf(EscanorUtil.underAttack))
              .replace("{1}", String.valueOf(EscanorUtil.botCounter))
      ));
    }

    if (ConfigUtil.isShow("attack", "cpu")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "cpu"),
          ConfigUtil.getName("attack", "cpu"),
          ConfigUtil.getValue("attack", "cpu").replace("{0}", String.valueOf(ServerUtil.getProcessCpuLoad()))
      ));
    }

    if (ConfigUtil.isShow("attack", "ram")) {
      builder.addField(new WebhookEmbed.EmbedField(
          ConfigUtil.isInline("attack", "ram"),
          ConfigUtil.getName("attack", "ram"),
          ConfigUtil.getValue("attack", "ram").replace("{0}", String.valueOf(ServerUtil.getMemory()))
      ));
    }
    if (ConfigUtil.getBoolean("attack.embed.timestamp")) {
      builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
    }
    builder.setFooter(new WebhookEmbed.EmbedFooter(ConfigUtil.getString("attack.embed.footer.text"), ConfigUtil.getString("attack.embed.footer.icon_url")));

    switch (type) {
      case SEND:
        try {
          this.client.send(builder.build()).thenAccept(readonlyMessage -> this.messageId = readonlyMessage.getId());
        } catch (HttpException exception) {
          EscanorWebhook.PLUGIN.getProxyServer().getLogger().warning("Failed to send webhook: " + exception.getMessage());
        }
        break;

      case SEND_AND_SET:
        try {
          this.client.send(builder.build()).thenRun(() -> this.messageId = -1L);
        } catch (HttpException exception) {
          EscanorWebhook.PLUGIN.getProxyServer().getLogger().warning("Failed to send webhook: " + exception.getMessage());
        }
        break;

      case EDIT:
        try {
          this.client.edit(this.messageId, builder.build());
        } catch (HttpException exception) {
          EscanorWebhook.PLUGIN.getProxyServer().getLogger().warning("Failed to send webhook: " + exception.getMessage());
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
    if (ConfigUtil.getBoolean("attack.extra.check_total_cps")) {
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
