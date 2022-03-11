package eu.mcprotection.escanorwebhook.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import eu.mcprotection.escanorwebhook.repository.ResourceRepository;
import net.md_5.bungee.config.Configuration;

public abstract class Webhook {

  protected WebhookClient client;
  protected Configuration config;

  public Webhook(ResourceRepository resourceRepository) {
    this.config = resourceRepository.get(ResourceRepository.MAIN_CONFIG_PATH);
  }

  public void connect(String webhookUrlPath, String threadName) {
    final WebhookClientBuilder builder =
        new WebhookClientBuilder(this.config.getString(webhookUrlPath));
    builder.setThreadFactory(
        job -> {
          final Thread thread = new Thread(job);
          thread.setName(threadName);
          thread.setDaemon(true);
          return thread;
        });
    builder.setWait(true);

    this.client = builder.build();
  }
}
