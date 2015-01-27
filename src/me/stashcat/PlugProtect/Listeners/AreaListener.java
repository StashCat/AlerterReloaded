package me.stashcat.PlugProtect.Listeners;

import java.util.HashMap;
import java.util.Map;

import me.stashcat.PlugProtect.Main;
import me.stashcat.PlugProtect.Events.PlayerEnterProtectedAreaEvent;
import me.stashcat.PlugProtect.Events.PlayerLeaveProtectedAreaEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AreaListener implements Listener {
	Main pl;
	Map<String, String> inArea = new HashMap<String, String>();
	
	public AreaListener(Main Main){
		pl = Main;
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (pl.settingArea.containsKey(e.getPlayer().getName()) && !e.isCancelled() && e.getClickedBlock() != null && e.getItem() != null){
			ItemStack i = e.getItem();
			if (i.isSimilar(pl.getWand())){
				Player p = e.getPlayer();
				Block b = e.getClickedBlock();
				Action a = e.getAction();
				if (pl.settingArea.containsKey(e.getPlayer().getName())){
					if (a == Action.LEFT_CLICK_BLOCK){
						pl.pos1.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 1 set.");
					} else if (a == Action.RIGHT_CLICK_BLOCK){
						pl.pos2.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 2 set.");
					}
					if (pl.pos1.containsKey(p.getName()) && pl.pos2.containsKey(p.getName())){
						pl.sendMsg(p, false, "You have selected both ends. Now execute &7/pp create [name]&r to save your area.");
					}
					e.setCancelled(true);
				} else if (pl.modifying.containsKey(e.getPlayer().getName())){
					if (a == Action.LEFT_CLICK_BLOCK){
						pl.pos1.remove(p.getName());
						pl.pos1.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 1 re-set.");
					} else if (a == Action.RIGHT_CLICK_BLOCK){
						pl.pos2.remove(p.getName());
						pl.pos2.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 2 re-set.");
					}
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent e){
		if (!(e.getDamager() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (pl.settingArea.containsKey(p.getName()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDeath(EntityDamageByEntityEvent e){
		if (!((Player)e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		pl.restoreInventory(p);
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
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		if (!e.isCancelled() && pl.settingArea.containsKey(e.getPlayer().getName())){
			pl.sendMsg(e.getPlayer(), false, "&cYou are setting an area.");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryMove(InventoryClickEvent e){
		if (!e.isCancelled() && pl.settingArea.containsKey(e.getWhoClicked().getName())){
			pl.sendMsg((Player)e.getWhoClicked(), false, "&cYou are setting an area.");
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		pl.restoreInventory(e.getPlayer());
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		Location loc = e.getTo();
		Location locFrom = e.getFrom();
		String key = pl.Areas.getArea(loc);
		String keyFrom = pl.Areas.getArea(locFrom);
		if (!inArea.containsKey(p.getName()) && pl.Areas.isProtected(loc)){
			inArea.put(p.getName(), key);
			pl.sendMsg(p, false, pl.Areas.getWelcomeMessage(key, pl.getCConfig().getString(key + ".owner")));
			PlayerEnterProtectedAreaEvent event = new PlayerEnterProtectedAreaEvent(p, key, pl.Areas.getWelcomeMessage(key, p.getName()));
			pl.getServer().getPluginManager().callEvent(event);
		} else if (inArea.containsKey(p.getName()) && inArea.get(p.getName()).equals(keyFrom) && !pl.Areas.isProtected(loc)) {
			inArea.remove(p.getName());
			pl.sendMsg(p, false, pl.Areas.getFarewellMessage(keyFrom, pl.getCConfig().getString(keyFrom + ".owner")));
			PlayerLeaveProtectedAreaEvent event = new PlayerLeaveProtectedAreaEvent(p, keyFrom, pl.Areas.getFarewellMessage(keyFrom, p.getName()));
			pl.getServer().getPluginManager().callEvent(event);
		} else if (inArea.containsKey(p.getName()) && !pl.Areas.isProtected(loc)) {
			inArea.remove(p.getName());
			PlayerLeaveProtectedAreaEvent event = new PlayerLeaveProtectedAreaEvent(p, null, null);
			pl.getServer().getPluginManager().callEvent(event);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getBlock().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis block is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getBlock().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis area is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockInteract(PlayerInteractEvent e){
		if (e.isCancelled())
			return;
		if (e.getClickedBlock() == null)
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getClickedBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getClickedBlock().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis block is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getRightClicked().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getRightClicked().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		if (e.isCancelled())
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		Player p = (Player)e.getDamager();
		if (pl.Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityTame(EntityTameEvent e){
		if (e.isCancelled())
			return;
		if (!(e.getOwner() instanceof Player))
			return;
		Player p = (Player) e.getOwner();
		if (pl.Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityLeash(PlayerLeashEntityEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.canBuild(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e){
		if (e.isCancelled())
			return;
		Entity ent = e.getEntity();
		Entity targ = e.getTarget();
		if (pl.Areas.getArea(ent.getLocation()) != null || pl.Areas.getArea(targ.getLocation()) != null){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEnterProtectedArea(PlayerMoveEvent e){
		if (e.isCancelled())
			return;
		String area = pl.Areas.getArea(e.getTo());
		Player p = e.getPlayer();
		if (pl.Areas.exists(area) && pl.Areas.isRestricted(area) && !pl.Areas.canBuild(area, p.getName())){
			Vector v = e.getTo().toVector().subtract(e.getFrom().toVector()).normalize();
			p.setVelocity(v.multiply(5));
			pl.sendMsg(p, false, "&cThis area is protected!");
		} else {
			pl.sendMsg(p, false, "else");
		}
	}
}
