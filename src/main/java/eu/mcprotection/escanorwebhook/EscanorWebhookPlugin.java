package eu.mcprotection.escanorwebhook;

import net.md_5.bungee.api.plugin.Plugin;

public class EscanorWebhookPlugin extends Plugin {
  @Override
  public void onLoad() {
    EscanorWebhook.PLUGIN.load(this);
  }

  @Override
  public void onEnable() {
    EscanorWebhook.PLUGIN.start();
  }

  @Override
  public void onDisable() {
    EscanorWebhook.PLUGIN.stop();
  }
}
