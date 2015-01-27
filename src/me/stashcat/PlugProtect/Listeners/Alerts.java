package me.stashcat.PlugProtect.Listeners;

import me.stashcat.PlugProtect.Main;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class Alerts implements Listener {
	Main pl;
	
	public Alerts(Main pl){
		this.pl = pl;
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e){
		String what = "BUCKET";
		if (e.getBucket() == Material.LAVA_BUCKET)
			what = "LAVA";
		else if (e.getBucket() == Material.WATER_BUCKET)
			what = "WATER";
		if (!pl.getConfig().getBoolean("warn." + what.toLowerCase()))
			return;
		System.out.println("[" + what + " PLACEMENT] " + e.getPlayer().getName() + " placed " + what + " at " + e.getBlockClicked().getX() + ", " + e.getBlockClicked().getY() + ", " + e.getBlockClicked().getZ());
		for (Player p : pl.getServer().getOnlinePlayers()){
			if (p.hasPermission("plugprotect.alert"))
				pl.sendMsg(p, true, e.getPlayer().getName() + " placed " + what + " at " + e.getBlockClicked().getX() + ", " + e.getBlockClicked().getY() + ", " + e.getBlockClicked().getZ());
		}
	}
}
