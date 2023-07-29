package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.exception.HttpException;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Singleton
public class ExceptionWebhook extends Webhook {

  @Inject private Plugin plugin;
  @Inject private ExecutorService service;

  @Inject
  private ExceptionWebhook(ResourceRepository resourceRepository) {
    super(resourceRepository);
  }

  public void connect() {
    super.connect("exception.url", "Exception-Webhook-Thread");
    this.plugin.getLogger().info("Connected to exception webhook");
  }

  public void send(@NotNull final String plugin, @NotNull final String stacktrace) {
    @NotNull String finalStacktrace =
        stacktrace.length() > 4096 ? stacktrace.substring(0, 4096) : stacktrace;
    this.service.submit(() -> {
      final WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
      builder.setTitle(
          new WebhookEmbed.EmbedTitle(
              this.getConfig().getString("exception.embed.title"),
              this.getConfig().getString("exception.embed.url")));
      builder.setColor(this.getConfig().getInt("exception.embed.color"));
      builder.setDescription(
          this.getConfig()
              .getString("exception.embed.description")
              .replace("{NL}", "\n")
              .replace("{0}", plugin)
              .replace("{1}", finalStacktrace));
      if (this.getConfig().getBoolean("exception.embed.timestamp")) {
        builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
      }
      builder.setFooter(
          new WebhookEmbed.EmbedFooter(
              this.getConfig().getString("exception.embed.footer.text"),
              this.getConfig().getString("exception.embed.footer.icon_url")));

      try {
        this.client.send(builder.build());
      } catch (HttpException exception) {
        this.plugin.getLogger().warning("Failed to send webhook: " + exception.getMessage());
      }
    });
  }
}
