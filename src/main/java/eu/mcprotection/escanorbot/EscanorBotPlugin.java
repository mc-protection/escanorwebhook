package eu.mcprotection.escanorbot;

import net.md_5.bungee.api.plugin.Plugin;

public class EscanorBotPlugin extends Plugin {
  @Override
  public void onLoad() {
    EscanorBot.PLUGIN.load(this);
  }

  @Override
  public void onEnable() {
    EscanorBot.PLUGIN.start();
  }

  @Override
  public void onDisable() {
    EscanorBot.PLUGIN.stop();
  }
}
