package me.stashcat.PlugProtect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.stashcat.PlugProtect.Events.PlayerEnterProtectedAreaEvent;
import me.stashcat.PlugProtect.Events.PlayerLeaveProtectedAreaEvent;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class MainListener implements Listener {
	Main pl;
	Map<String, String> inArea = new HashMap<String, String>();
	
	public MainListener(Main Main){
		pl = Main;
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (!e.isCancelled() && e.getClickedBlock() != null){
			ItemStack i = e.getItem();
			if (i.isSimilar(pl.getWand())){
				Player p = e.getPlayer();
				Block b = e.getClickedBlock();
				Action a = e.getAction();
				if (pl.setting.contains(e.getPlayer().getName())){
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
						pl.pos1.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 1 re-set.");
					} else if (a == Action.RIGHT_CLICK_BLOCK){
						pl.pos2.put(p.getName(), b.getLocation());
						pl.sendMsg(p, false, "Position 2 re-set.");
					}
				}
			}
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
		if (!e.isCancelled() && pl.setting.contains(e.getPlayer().getName())){
			ItemStack i = e.getItemDrop().getItemStack();
			if (i.isSimilar(pl.getWand())){
				pl.sendMsg(e.getPlayer(), false, "&cYou are not allowed to drop the wand.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInvMove(InventoryClickEvent e){
		Player p = (Player)e.getWhoClicked();
		if (pl.setting.contains(p.getName())){
			ItemStack i = e.getCurrentItem();
			if (i.isSimilar(pl.getWand())){
				pl.sendMsg(p, false, "&cYou are not allowed to move the wand.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInvDrag(InventoryDragEvent e){
		Player p = (Player)e.getWhoClicked();
		if (pl.setting.contains(p.getName())){
			Collection<ItemStack> icoll = e.getNewItems().values();
			for (ItemStack i : icoll){
				if (i.isSimilar(pl.getWand())){
					pl.sendMsg(p, false, "&cYou are not allowed to move the wand.");
					e.setCancelled(true);
				}
			}
		}
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
			pl.sendMsg(p, false, pl.Areas.getEnterMessage(key, pl.getCConfig().getString(key + ".owner")));
			PlayerEnterProtectedAreaEvent event = new PlayerEnterProtectedAreaEvent(p, key, pl.Areas.getLeaveMessage(key, p.getName()));
			pl.getServer().getPluginManager().callEvent(event);
		} else if (inArea.containsKey(p.getName()) && inArea.get(p.getName()).equals(keyFrom) && !pl.Areas.isProtected(loc)) {
			inArea.remove(p.getName());
			pl.sendMsg(p, false, pl.Areas.getLeaveMessage(keyFrom, pl.getCConfig().getString(keyFrom + ".owner")));
			PlayerLeaveProtectedAreaEvent event = new PlayerLeaveProtectedAreaEvent(p, keyFrom, pl.Areas.getLeaveMessage(keyFrom, p.getName()));
			pl.getServer().getPluginManager().callEvent(event);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (pl.Areas.isProtected(e.getBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getBlock().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getBlock().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getClickedBlock().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getRightClicked().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
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
			if (!pl.Areas.isOwner(pl.Areas.getArea(e.getEntity().getLocation()), p.getName())){
				pl.sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSignPlace(BlockPlaceEvent e){
		if (e.isCancelled())
			return;
		Block b = e.getBlock();
		if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN){
			Sign s = (Sign)b;
			Player p = e.getPlayer();
			String[] l = s.getLines();
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[private]")){
				s.setLine(0, "[" + ChatColor.GREEN + "Private" + ChatColor.RESET + "]");
				if (ChatColor.stripColor(l[1]).isEmpty()){
					s.setLine(1, p.getName());
				}
			}
		}
	}
	
	@EventHandler
	public void onSignDestroy(BlockBreakEvent e){
		if (e.isCancelled())
			return;
		Block b = e.getBlock();
		if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN){
			Sign s = (Sign)b;
			String[] l = s.getLines();
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[private]")){
				
			}
		}
	}
}
