package me.stashcat.PlugProtect.Listeners;

import me.stashcat.PlugProtect.Main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat implements Listener {
	Main pl;
	
	public Chat(Main pl){
		this.pl = pl;
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.settingWelcome.containsKey(p.getName()) || pl.settingFarewell.containsKey(p.getName())){
			boolean welcome = pl.settingWelcome.containsKey(p.getName());
			String msg = e.getMessage();
			String setting = null;
			String area = null;
			if (welcome){
				setting = "Welcome";
				area = pl.settingWelcome.get(p.getName());
				pl.settingWelcome.remove(p.getName());
			} else {
				setting = "Farewell";
				area = pl.settingFarewell.get(p.getName());
				pl.settingFarewell.remove(p.getName());
			}
			pl.getCConfig().set(area + "." + setting.toLowerCase(), msg);
			pl.saveCConfig();
			pl.sendMsg(p, false, "&a" + setting + " message successfully set to:");
			pl.sendMsg(p, false, "  " + msg);
			e.setCancelled(true);
		}
	}
}
