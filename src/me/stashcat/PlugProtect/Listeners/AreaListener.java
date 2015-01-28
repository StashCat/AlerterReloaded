package me.stashcat.PlugProtect.Listeners;

import java.util.HashMap;
import java.util.Map;

import me.stashcat.PlugProtect.Main;
import me.stashcat.PlugProtect.Events.PlayerEnterProtectedAreaEvent;
import me.stashcat.PlugProtect.Events.PlayerLeaveProtectedAreaEvent;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class AreaListener extends Main implements Listener {
	Map<String, String> inArea = new HashMap<String, String>();
	Main pl;
	Wand Wand;
	Alerts Alerts;
	Chat Chat;
	Signs Signs;
	
	public AreaListener(Main Main){
		pl = Main;
		getServer().getPluginManager().registerEvents(this, pl);
		Wand = new Wand(pl);
		Alerts = new Alerts(pl);
		Chat = new Chat(pl);
		if (getConfig().getBoolean("modules.signs"))
			Signs = new Signs(pl);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		Location loc = e.getTo();
		Location locFrom = e.getFrom();
		String key = Areas.getArea(loc);
		String keyFrom = Areas.getArea(locFrom);
		if (!inArea.containsKey(p.getName()) && Areas.isProtected(loc)){
			inArea.put(p.getName(), key);
			sendMsg(p, false, Areas.getWelcomeMessage(key, getCConfig().getString(key + ".owner")));
			PlayerEnterProtectedAreaEvent event = new PlayerEnterProtectedAreaEvent(p, key, Areas.getWelcomeMessage(key, p.getName()));
			getServer().getPluginManager().callEvent(event);
		} else if (inArea.containsKey(p.getName()) && inArea.get(p.getName()).equals(keyFrom) && !Areas.isProtected(loc)) {
			inArea.remove(p.getName());
			sendMsg(p, false, Areas.getFarewellMessage(keyFrom, getCConfig().getString(keyFrom + ".owner")));
			PlayerLeaveProtectedAreaEvent event = new PlayerLeaveProtectedAreaEvent(p, keyFrom, Areas.getFarewellMessage(keyFrom, p.getName()));
			getServer().getPluginManager().callEvent(event);
		} else if (inArea.containsKey(p.getName()) && !Areas.isProtected(loc)) {
			inArea.remove(p.getName());
			PlayerLeaveProtectedAreaEvent event = new PlayerLeaveProtectedAreaEvent(p, null, null);
			getServer().getPluginManager().callEvent(event);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (Areas.isProtected(e.getBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getBlock().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis block is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (Areas.isProtected(e.getBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getBlock().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis area is protected.");
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
		if (Areas.isProtected(e.getClickedBlock().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getClickedBlock().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis block is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (Areas.isProtected(e.getRightClicked().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getRightClicked().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis entity is protected.");
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
		if (Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getEntity().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis entity is protected.");
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
		if (Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getEntity().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis entity is protected.");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityLeash(PlayerLeashEntityEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (Areas.isProtected(e.getEntity().getLocation()) && !p.hasPermission("plugprotect.bypass")){
			if (!Areas.canBuild(Areas.getArea(e.getEntity().getLocation()), p.getName())){
				sendMsg(p, false, "&cThis entity is protected.");
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
		if (Areas.getArea(ent.getLocation()) != null || Areas.getArea(targ.getLocation()) != null){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEnterProtectedArea(PlayerMoveEvent e){
		if (e.isCancelled())
			return;
		String area = Areas.getArea(e.getTo());
		Player p = e.getPlayer();
		if (Areas.exists(area) && Areas.isRestricted(area) && !Areas.canBuild(area, p.getName())){
			Vector v = e.getTo().toVector().subtract(e.getFrom().toVector()).normalize();
			p.setVelocity(v.multiply(5));
			sendMsg(p, false, "&cThis area is protected!");
		} else {
			sendMsg(p, false, "else");
		}
	}
}
