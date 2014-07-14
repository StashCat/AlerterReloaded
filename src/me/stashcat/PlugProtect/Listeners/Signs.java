package me.stashcat.PlugProtect.Listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import me.stashcat.PlugProtect.Main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Signs implements Listener {
	Main pl;

	public Signs(Main Main){
		pl = Main;
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onSignPlace(SignChangeEvent e){
		if (e.isCancelled())
			return;
		Block b = e.getBlock();
		if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN){
			Sign s = (Sign)b.getState();
			Player p = e.getPlayer();
			String[] l = e.getLines();
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[Private]")){
				e.setLine(0, "[" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Private" + ChatColor.RESET + "]");
				if (ChatColor.stripColor(l[1]).isEmpty())
					e.setLine(1, p.getName());
				s.update();
			}
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[More]")){
				e.setLine(0, "[" + ChatColor.DARK_GREEN + ChatColor.BOLD + "More" + ChatColor.RESET + "]");
				s.update();
			}
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[Sell]")){
				
			}
		}
	}
	
	@EventHandler
	public void onSignDestroy(BlockBreakEvent e){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		Block b = e.getBlock();
		if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN){
			Sign s = (Sign)b.getState();
			String[] l = s.getLines();
			if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[Private]") || ChatColor.stripColor(l[0]).equalsIgnoreCase("[More]") || ChatColor.stripColor(l[0]).equalsIgnoreCase("[Sell]")){
				if (!Arrays.asList(l).contains(p.getName()) && !p.hasPermission("plugprotect.bypass")){
					pl.sendMsg(p, false, "&cThis sign is protected!");
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDoorOpen(PlayerInteractEvent e){
		if (!e.isCancelled() && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.WOODEN_DOOR){
			Block b = e.getClickedBlock();
			Player p = e.getPlayer();
			if (b.getRelative(BlockFace.UP).getType() == Material.WOOD_DOOR)
				b = b.getRelative(BlockFace.UP);
			Location loc = b.getLocation();
			double y = loc.getY();
			Set<Sign> signs = new HashSet<Sign>();
			for (double yl = y - 2; yl <= y + 3; yl++){
				loc.setY(yl);
				Set<Block> ba = pl.getBlocksAround(loc);
				for (Block bl : ba){
					if (bl.getType() == Material.WALL_SIGN || bl.getType() == Material.SIGN_POST){
						signs.add((Sign)bl.getState());
					}
				}
			}
			if (!signs.isEmpty()){
				for (Object sign : signs){
					Sign s = (Sign)sign;
					String[] l = s.getLines();
					if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[Private]")){
						if (!p.hasPermission("plugprotect.bypass"))
							e.setCancelled(true);
						if (Arrays.asList(l).contains(p.getName())){
							e.setCancelled(false);
						}
					}
					if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[More]") && e.isCancelled()){
						if (Arrays.asList(l).contains(p.getName())){
							e.setCancelled(false);
						}
					}
				}
				if (p.hasPermission("plugprotect.bypass")){
					pl.sendMsg(p, false, "&cWARNING: You are accessing a protected door.");
					e.setCancelled(false);
				}
				if (e.isCancelled())
					pl.sendMsg(p, false, "&cThis door is protected.");
			}
		}
	}
	
	@EventHandler
	public void openContainer(PlayerInteractEvent e){
		if (!e.isCancelled() && e.getClickedBlock() != null && (e.getClickedBlock().getType() == Material.CHEST || e.getClickedBlock().getType() == Material.TRAPPED_CHEST || e.getClickedBlock().getType() == Material.FURNACE)){
			Block b = e.getClickedBlock();
			Player p = e.getPlayer();
			Location loc = b.getLocation();
			double y = loc.getY();
			Set<Sign> signs = new HashSet<Sign>();
			for (double yl = y - 1; yl <= y + 1; yl++){
				loc.setY(yl);
				Set<Block> ba = pl.getBlocksAround(loc);
				for (Block bl : ba){
					if (bl.getType() == Material.WALL_SIGN || bl.getType() == Material.SIGN_POST){
						signs.add((Sign)bl.getState());
					}
				}
			}
			if (!signs.isEmpty()){
				for (Object sign : signs){
					Sign s = (Sign)sign;
					String[] l = s.getLines();
					if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[Private]")){
						if (!p.hasPermission("plugprotect.bypass"))
							e.setCancelled(true);
						if (Arrays.asList(l).contains(p.getName())){
							e.setCancelled(false);
						}
					}
					if (ChatColor.stripColor(l[0]).equalsIgnoreCase("[More]") && e.isCancelled()){
						if (Arrays.asList(l).contains(p.getName())){
							e.setCancelled(false);
						}
					}
				}
				if (p.hasPermission("plugprotect.bypass")){
					pl.sendMsg(p, false, "&cWARNING: You are accessing a protected container.");
					e.setCancelled(false);
				}
				if (e.isCancelled())
					pl.sendMsg(p, false, "&cThis chest is container.");
			}
		}
	}
}
