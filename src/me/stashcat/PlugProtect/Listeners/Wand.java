package me.stashcat.PlugProtect.Listeners;

import me.stashcat.PlugProtect.Main;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class Wand extends Main implements Listener {
	Main pl;
	
	public Wand(Main pl){
		this.pl = pl;
		getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (settingArea.containsKey(e.getPlayer().getName()) && !e.isCancelled() && e.getClickedBlock() != null && e.getItem() != null){
			ItemStack i = e.getItem();
			if (i.isSimilar(getWand())){
				Player p = e.getPlayer();
				Block b = e.getClickedBlock();
				Action a = e.getAction();
				if (settingArea.containsKey(e.getPlayer().getName())){
					if (a == Action.LEFT_CLICK_BLOCK){
						pos1.put(p.getName(), b.getLocation());
						sendMsg(p, false, "Position 1 set.");
					} else if (a == Action.RIGHT_CLICK_BLOCK){
						pos2.put(p.getName(), b.getLocation());
						sendMsg(p, false, "Position 2 set.");
					}
					if (pos1.containsKey(p.getName()) && pos2.containsKey(p.getName())){
						sendMsg(p, false, "You have selected both ends. Now execute &7/pp create [name]&r to save your area.");
					}
					e.setCancelled(true);
				} else if (modifying.containsKey(e.getPlayer().getName())){
					if (a == Action.LEFT_CLICK_BLOCK){
						pos1.remove(p.getName());
						pos1.put(p.getName(), b.getLocation());
						sendMsg(p, false, "Position 1 re-set.");
					} else if (a == Action.RIGHT_CLICK_BLOCK){
						pos2.remove(p.getName());
						pos2.put(p.getName(), b.getLocation());
						sendMsg(p, false, "Position 2 re-set.");
					}
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		if (!e.isCancelled() && settingArea.containsKey(e.getPlayer().getName())){
			sendMsg(e.getPlayer(), false, "&cYou are setting an area.");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryMove(InventoryClickEvent e){
		if (!e.isCancelled() && settingArea.containsKey(e.getWhoClicked().getName())){
			sendMsg((Player)e.getWhoClicked(), false, "&cYou are setting an area.");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		restoreInventory(e.getPlayer());
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e){
		if (!(e.getDamager() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (settingArea.containsKey(p.getName()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDeath(EntityDamageByEntityEvent e){
		if (!((Player)e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		restoreInventory(p);
	}
}
