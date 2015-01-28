package me.stashcat.PlugProtect.Listeners;

import me.stashcat.PlugProtect.Main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat extends Main implements Listener {
	Main pl;
	
	public Chat(Main pl){
		this.pl = pl;
		getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (settingWelcome.containsKey(p.getName()) || settingFarewell.containsKey(p.getName())){
			boolean welcome = settingWelcome.containsKey(p.getName());
			String msg = e.getMessage();
			String setting = null;
			String area = null;
			if (welcome){
				setting = "Welcome";
				area = settingWelcome.get(p.getName());
				settingWelcome.remove(p.getName());
			} else {
				setting = "Farewell";
				area = settingFarewell.get(p.getName());
				settingFarewell.remove(p.getName());
			}
			getCConfig().set(area + "." + setting.toLowerCase(), msg);
			saveCConfig();
			sendMsg(p, false, "&a" + setting + " message successfully set to:");
			sendMsg(p, false, "  " + msg);
			e.setCancelled(true);
		}
	}
}
