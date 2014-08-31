package me.stashcat.PlugProtect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.stashcat.PlugProtect.Listeners.Signs;
import me.stashcat.PlugProtect.Metrics.Metrics;
import me.stashcat.PlugProtect.Updater.Updater;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
	
	private String prefix;
	public MainListener MainListener;
	Map<String, ItemStack[]> settingArea = new HashMap<String, ItemStack[]>();
	Map <String, ItemStack[]> armour = new HashMap <String, ItemStack[]>();
	Map<String, String> settingWelcome = new HashMap<String, String>();
	Map<String, String> settingFarewell = new HashMap<String, String>();
	Map<String, String> modifying = new HashMap<String, String>();
	Map<String, Location> pos1 = new HashMap<String, Location>();
	Map<String, Location> pos2 = new HashMap<String, Location>();
	private FileConfiguration data = null;
	private File dataFile = null;
	private ItemStack wand;
	public Areas Areas;
	public Signs Signs;
	Metrics Metrics;
	
	public void onEnable(){
		saveDefaultConfig();
		saveConfig();
		saveDefaultCConfig();
		saveCConfig();
		if (getConfig().getBoolean("auto-update")){
			@SuppressWarnings("unused")
			Updater updater = new Updater(this, 82179, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
		}
		initWand();
		if (getConfig().getBoolean("send-stats"))
			try {
				Metrics = new Metrics(this);
				Metrics.start();
				getLogger().info("Metrics successfully started!");
			} catch (IOException e) {
				getLogger().warning("Could not start Metrics!");
				getLogger().warning("\"" + e.getMessage() + "\"");
			}
		else
			getLogger().info("Metrics not starting, disabled in config :(");
		Areas = new Areas(this);
		MainListener = new MainListener(this);
		Signs = new Signs(this);
		prefix = "[" + ChatColor.GREEN + ChatColor.BOLD + getDescription().getName() + ChatColor.RESET + "] ";
		getLogger().info(getDescription().getFullName() + " Enabled!");
	}
	
	public void onDisable(){
		getLogger().info(getDescription().getFullName() + " Disabled!");
		restoreAllInventories();
	}
	
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("pp") || cmd.getName().equalsIgnoreCase("plugprotect")){
			if (args.length == 0){
				sendMsg(s, false, "&2" + getDescription().getFullName() + "&r by " + getDescription().getAuthors());
				sendMsg(s, false, "Use &a/pp help&r to see all commands.");
				sendMsg(s, false, "In memory of the original Alerter plugin by &apatrick_pk91&r");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("help") && s.hasPermission("plugprotect")){
				sendMsg(s, false, "-= &3&lPlugProtect&3 Help&r =-");
				if (s.hasPermission("plugprotect")) sendMsg(s, false, "&a/pp &2help&r  - Displays help about all of the commands");
				if (s.hasPermission("plugprotect.reload")) sendMsg(s, false, "&a/pp &2reload&r  - Reloads the config");
				if (s.hasPermission("plugprotect.set")) sendMsg(s, false, "&a/pp &2set&r  - Sets a region to protect");
				if (s.hasPermission("plugprotect.set")) sendMsg(s, false, "&a/pp &2create [area_name]&r  - Creates an area if set");
				if (s.hasPermission("plugprotect.set")) sendMsg(s, false, "&a/pp &2cancel&r  - Cancels the setting of an area");
				if (s.hasPermission("plugprotect.rename")) sendMsg(s, false, "&a/pp &2rename [current_name] [new_name]&r  - Renames an area");
				if (s.hasPermission("plugprotect.delete")) sendMsg(s, false, "&a/pp &2delete [area_name]&r  - Deletes an area");
				if (s.hasPermission("plugprotect.list") && !s.hasPermission("plugprotect.list.other")) sendMsg(s, false, "&a/pp &2list&r  - Lists all areas you own");
				else if (s.hasPermission("plugprotect.list")) sendMsg(s, false, "&a/pp &2list (all)&r  - Lists areas you own or all existing areas.");
				if (s.hasPermission("plugprotect.modify")) sendMsg(s, false, "&a/pp &2modify [area_name]&r  - Modifies the points of an area");
				if (s.hasPermission("plugprotect.warp")) sendMsg(s, false, "&a/pp &2warp [area_name]&r  - Warps to an area");
				if (s.hasPermission("plugprotect.setwarp")) sendMsg(s, false, "&a/pp &2setwarp&r  - Sets a warp for an area");
				if (s.hasPermission("plugprotect.whitelist")) sendMsg(s, false, "&a/pp &2[add/remove] [player_name] [area_name]&r  - Adds/removes a player to/from your area's whitelist");
				if (s.hasPermission("plugprotect.setmsg")) sendMsg(s, false, "&a/pp &2[setwelcome/setfarewell] [area_name]&r  - Sets a welcome/farewell message for an area");
				//if (s.hasPermission("plugprotect.togglepvp")) sendMsg(s, false, "&a/pp &2togglepvp [area_name}&r  - Toggles PvP in your area");
				sendMsg(s, false, "-= &cEnd&r =-");
				return true;
				//if (s.hasPermission("plugprotect.PERM")) sendMsg(s, false, "&a/pp &2ARG&r  - DESC");
			} else if (args.length == 1 && args[0].equalsIgnoreCase("reload") && s.hasPermission("plugprotect.reload")){
				reloadConfig();
				reloadCConfig();
				sendMsg(s, false, "Config reloaded");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("set") && s.hasPermission("plugprotect.set")){
				if (!(s instanceof Player)){sendMsg(s, false, "You must be a player to execute this command."); return true;}
				Player p = (Player)s;
				if (settingArea.containsKey(p.getName()) || modifying.containsKey(p.getName())){
					sendMsg(p, false, "&cYou are already setting an area!");
					return true;
				}
				settingArea.put(p.getName(), p.getInventory().getContents().clone());
				armour.put(p.getName(), p.getInventory().getArmorContents().clone());
				p.getInventory().clear();
				p.getInventory().setArmorContents(null);
				p.getInventory().setItem(0, wand);
				sendMsg(p, false, "Please select both ends of the area you want to protect with the golden axe.");
				sendMsg(p, false, "Left-click sets position 1, while right-click sets position 2.");
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("create") && s.hasPermission("plugprotect.set")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				if (settingArea.containsKey(s.getName()) && pos1.containsKey(s.getName()) && pos2.containsKey(s.getName()) && getCConfig().get(args[1]) == null){
					if (pos1.get(s.getName()).getWorld() != pos2.get(s.getName()).getWorld()){
						sendMsg(s, false, "&cWorlds of both points must match!");
						return true;
					}
					Player p = (Player)s;
					p.getInventory().clear();
					p.getInventory().setContents(settingArea.get(p.getName()));
					p.getInventory().setArmorContents(armour.get(p.getName()));
					getCConfig().set(args[1] + ".owner", s.getName());
					getCConfig().set(args[1] + ".world", pos1.get(s.getName()).getWorld().getName());
					getCConfig().set(args[1] + ".pos1", pos1.get(s.getName()).getX() + "," + pos1.get(s.getName()).getZ());
					getCConfig().set(args[1] + ".pos2", pos2.get(s.getName()).getX() + "," + pos2.get(s.getName()).getZ());
					saveCConfig();
					settingArea.remove(s.getName());
					armour.remove(s.getName());
					pos1.remove(s.getName());
					pos2.remove(s.getName());
					sendMsg(s, false, "&aSelected area created as &2" + args[1] + "&a.");
					return true;
				} else {
					if (!settingArea.containsKey(s.getName()))
						sendMsg(s, false, "&cYou are not setting an area.");
					else if (!pos1.containsKey(s.getName()) && pos2.containsKey(s.getName()))
						sendMsg(s, false, "&cYou did not set all points.");
					return true;
				}
			} else if (args.length == 1 && args[0].equalsIgnoreCase("create") && s.hasPermission("plugprotect.set")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				sendMsg(s, false, "&cYou must enter your area name.");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("cancel") && s.hasPermission("plugprotect.set")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				Player p = (Player)s;
				if (settingArea.containsKey(s.getName())){
					pos1.remove(s.getName());
					pos2.remove(s.getName());
					p.getInventory().clear();
					p.getInventory().setContents(settingArea.get(p.getName()));
					p.getInventory().setArmorContents(armour.get(p.getName()));
					settingArea.remove(s.getName());
					armour.remove(p.getName());
					sendMsg(s, false, "&aArea setting cancelled.");
				} else {
					sendMsg(s, false, "&cYou are not setting an area.");
				}
				return true;
			} else if (args.length == 3 && args[0].equalsIgnoreCase("rename") && s.hasPermission("plugprotect.rename")){
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.rename.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				if (getCConfig().getString(args[2]) != null){
					sendMsg(s, false, "&cArea &a" + args[2] + "&c already exists.");
					return false;
				}
				getCConfig().set(args[2], getCConfig().getConfigurationSection(args[1]));
				getCConfig().set(args[1], null);
				saveCConfig();
				sendMsg(s, false, "&aArea successfully &6renamed&a to &2" + args[2] + "&a.");
				return true;
			} else if (args.length <= 2 && args.length > 0 && args[0].equalsIgnoreCase("rename") && s.hasPermission("plugprotect.rename")){
				sendMsg(s, false, "&cYou must enter both the current and new area names!");
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("delete") && s.hasPermission("plugprotect.delete")){
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.delete.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				getCConfig().set(args[1], null);
				saveCConfig();
				sendMsg(s, false, "&aArea &2" + args[1] + "&a successfully &cdeleted&a.");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("delete") && s.hasPermission("plugprotect.delete")){
				sendMsg(s, false, "&cYou must enter the area name to delete!");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("list") && s.hasPermission("plugprotect.list") || args.length == 2 && args[0].equalsIgnoreCase("list") && s.hasPermission("plugprotect.list")){
				int page;
				if (args.length == 2){
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e){
						sendMsg(s, false, "&cPage is not a number!");
						return true;
					}
				} else {
					page = 0;
				}
				page *= 5;
				int results = 5;
				Iterator<String> keys = getCConfig().getConfigurationSection("").getKeys(false).iterator();
				for (int i = 0; i <= page + results;){
					String key = null;
					if (i < page && keys.hasNext()){
						keys.next();
						continue;
					} else if ((i < page || i == 0) && !keys.hasNext()){
						sendMsg(s, false, "&cPage does not exist.");
						break;
					} else if (!keys.hasNext()){
						break;
					} else {
						key = keys.next();
					}
					if (key == null)
						continue;
					if (Areas.isOwner(key, s.getName()) || s.hasPermission("plugprotect.list.other")){
						sendMsg(s, false, "&a" + key + "&r: (owned by &a" + getCConfig().getString(key + ".owner") + "&r)");
						double[] pos = Areas.getWarp(key);
						sendMsg(s, false, " Position: &ax &2" + pos[0] + "&r, &az &2" + pos[1]);
						sendMsg(s, false, " Size: &a" + Areas.getSize(key));
						i++;
					}
					if (i <= page + results - 1 && keys.hasNext()){
						sendMsg(s, false, "Type in &a/pp list " + (page / 5) + "&r to view more.");
						break;
					}
				}
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("modify") && s.hasPermission("plugprotect.modify")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				Player p = (Player)s;
				if (args[1].equalsIgnoreCase("confirm") && modifying.containsKey(s.getName())){
					if (pos1.get(s.getName()).getWorld() != pos2.get(s.getName()).getWorld()){
						sendMsg(s, false, "&cWorlds of both points must match!");
						return true;
					}
					ItemStack i = ((Player)s).getItemInHand();
					if (!i.isSimilar(wand)){
						sendMsg(s, false, "&cYou must be holding the wand to save!");
						return true;
					}
					((Player)s).getInventory().clear(((Player)s).getInventory().getHeldItemSlot());
					getCConfig().set(modifying.get(s.getName()) + ".pos1", pos1.get(s.getName()).getX() + "," + pos1.get(s.getName()).getZ());
					getCConfig().set(modifying.get(s.getName()) + ".pos2", pos2.get(s.getName()).getX() + "," + pos2.get(s.getName()).getZ());
					saveCConfig();
					pos1.remove(s.getName());
					pos2.remove(s.getName());
					sendMsg(s, false, "&aSaved modification of &2" + modifying.get(s.getName()) + "&a.");
					modifying.remove(s.getName());
					return true;
				}
				if (settingArea.containsKey(p.getName()) || modifying.containsKey(p.getName())){
					sendMsg(p, false, "&cYou are already setting an area!");
					return true;
				}
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.modify.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				if (p.getInventory().getItemInHand().getType() == Material.AIR){
					p.getInventory().setItemInHand(wand);
					modifying.put(p.getName(), args[1]);
					pos1.put(p.getName(), Areas.getPosLocs(args[1], 1));
					pos2.put(p.getName(), Areas.getPosLocs(args[1], 2));
					sendMsg(p, false, "Please select the both ends of your new area.");
					sendMsg(p, false, "Left-click sets position 1, while right-click sets position 2.");
					sendMsg(p, false, "Once you're finished, type &7/pp modify confirm&r to save your modification.");
				} else {
					sendMsg(p, false, "&cPlease remove any items you are currently holding and try again.");
				}
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("modify") && s.hasPermission("plugprotect.modify")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				sendMsg(s, false, "&cYou must enter the name of an area to modify!");
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("warp") && s.hasPermission("plugprotect.warp")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				Player p = ((Player)s);
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.warp.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				double[] warp = Areas.getWarp(args[1]);
				Location loc = new Location(p.getWorld(), warp[0], 0, warp[1]);
				loc.setY(p.getWorld().getHighestBlockYAt(loc));
				p.teleport(loc, TeleportCause.PLUGIN);
				sendMsg(s, false, "&aSuccessfully warped to &2" + args[1] + "&a.");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("warp") && s.hasPermission("plugprotect.warp")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				sendMsg(s, false, "&cYou must specify what area to warp to!");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("setwarp") && s.hasPermission("plugprotect.setwarp")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				Player p = (Player)s;
				if (Areas.getArea(p.getLocation()) == null){
					sendMsg(s, false, "&cYou must be standing inside an area to set it's warp!");
					return true;
				}
				if (!Areas.isOwner(Areas.getArea(p.getLocation()), s.getName()) && !s.hasPermission("plugprotect.setwarp.other")){
					sendMsg(s, false, "&cThis area &a" + Areas.getArea(p.getLocation()) + "&c does not belong to you.");
					return true;
				}
				getCConfig().set(Areas.getArea(p.getLocation()) + ".warp", p.getLocation().getX() + "," + p.getLocation().getZ());
				saveCConfig();
				sendMsg(s, false, "&aCustom warp set for area &2" + Areas.getArea(p.getLocation()) + "&a!");
				return true;
			} else if (args.length == 3 && args[0].equalsIgnoreCase("add") && s.hasPermission("plugprotect.whitelist")
					|| args.length == 3 && args[0].equalsIgnoreCase("remove") && s.hasPermission("plugprotect.whitelist")){
				boolean add = args[0].equalsIgnoreCase("add");
				if (Areas.exists(args[2])){
					sendMsg(s, false, "&cArea &a" + args[2] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[2], s.getName()) && !s.hasPermission("plugprotect.whitelist.other")){
					sendMsg(s, false, "&cThe area &a" + args[2] + "&c does not belong to you.");
					return true;
				}
				editList(getCConfig(), add, args[2] + ".whitelist", args[1]);
				saveCConfig();
				String action = null;
				if (add)
					action = "added to";
				else
					action = "removed from";
				sendMsg(s, false, "&aPlayer &2" + args[1] + "&a has been " + action + " the whitelist!");
				return true;
			} else if (args.length > 0 && args.length < 3 && args[0].equalsIgnoreCase("add") && s.hasPermission("plugprotect.whitelist")
					|| args.length > 0 && args.length < 3 && args[0].equalsIgnoreCase("remove") && s.hasPermission("plugprotect.whitelist")){
				boolean add = args[0].equalsIgnoreCase("add");
				String action = null;
				if (add)
					action = "add to";
				else
					action = "remove from";
				sendMsg(s, false, "&cYou must enter both the area name and the player to " + action + " the whitelist!");
				return true;
			} else if (args.length == 2 && (args[0].equalsIgnoreCase("setwelcome") || args[0].equalsIgnoreCase("setfarewell")) && s.hasPermission("plugprotect.setmsg")){
				if (!(s instanceof Player)){sendMsg(s, false, "&cYou must be a player to execute this command."); return true;}
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.setmsg.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				boolean welcome = args[0].equalsIgnoreCase("setwelcome");
				String setting = null;
				if (welcome) setting = "welcome"; else setting = "farewell";
				if (welcome)
					settingWelcome.put(s.getName(), args[1]);
				else
					settingFarewell.put(s.getName(), args[1]);
				sendMsg(s, false, "Please enter your " + setting + " message in chat.");
				return true;
			} else if (args.length == 1 && (args[0].equalsIgnoreCase("setwelcome") || args[0].equalsIgnoreCase("setfarewell")) && s.hasPermission("plugprotect.setmsg")){
				boolean welcome = args[0].equalsIgnoreCase("setwelcome");
				String setting = null;
				if (welcome) setting = "welcome"; else setting = "farewell";
				sendMsg(s, false, "&cYou must specify an area to set the " + setting + " message of!");
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("restrictentry")){
				if (Areas.exists(args[1])){
					sendMsg(s, false, "&cArea &a" + args[1] + "&c does not exist.");
					return true;
				}
				if (!Areas.isOwner(args[1], s.getName()) && !s.hasPermission("plugprotect.setmsg.other")){
					sendMsg(s, false, "&cThe area &a" + args[1] + "&c does not belong to you.");
					return true;
				}
				getCConfig().set(args[1] + ".restricted", !Areas.isRestricted(args[1]));
				saveCConfig();
				String restrict = null;
				if (Areas.isRestricted(args[1]))
					restrict = "restricted";
				else
					restrict = "unrestricted";
				sendMsg(s, false, "&aSuccessfully set &2" + args[1] + "&a to &2" + restrict + "&a!");
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("restrictentry")){
				sendMsg(s, false, "&cYou must specify the area you want to restrict entry of!");
				return true;
			}
			sendMsg(s, false, "&cInvalid arguments! Use &r/pp help&c to see help.");
			return true;
		}
		return false;
	}
	
	public FileConfiguration getCConfig() {
        if (data == null) {
            reloadCConfig();
        }
        return data;
    }
    
    public void saveCConfig(){
        if (data == null || dataFile == null) {
        return;
        }
            try {
				getCConfig().save(dataFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    
    public void reloadCConfig() {
        if (dataFile == null)
        dataFile = new File(getDataFolder(), "data.yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = getResource("data.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            data.setDefaults(defConfig);
        }
    }
    
    public void saveDefaultCConfig() {
        if (dataFile == null) {
            dataFile = new File(getDataFolder(), "data.yml");
        }
        if (!dataFile.exists()) {            
             saveResource("data.yml", false);
         }
    }
	
	public void sendMsg(CommandSender s, boolean usePrefix, String msg){
		msg = colorize(msg);
		if (usePrefix)
			s.sendMessage(prefix + msg);
		else
			s.sendMessage(msg);
	}
    
    public int digitize(String amount){
    	int integer;
    	try{
			integer = Integer.parseInt(amount);
			return integer;
			} catch (NumberFormatException e) {
				return -1;
			}
    }
    
    public String colorize(String msg){
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
    
    public String colorize(String msg, char character){
    	return ChatColor.translateAlternateColorCodes(character, msg);
    }
    
    public void editList(FileConfiguration conf, boolean add, String key, String... element) {
        List<String> list = conf.getStringList(key);
        if(add)
        	list.addAll(Arrays.asList(element));
        else
        	list.removeAll(Arrays.asList(element));
        conf.set(key, list);
    }
    
    void initWand(){
    	wand = new ItemStack(Material.GOLD_AXE);
		ItemMeta meta = wand.getItemMeta();
		meta.setDisplayName(colorize("&3&lMagic Wand"));
		meta.setLore(Arrays.asList(colorize("&r&aLeft-click to set position 1."), colorize("&r&aRight-click to set position 2.")));
		wand.setItemMeta(meta);
    }
    
    public ItemStack getWand(){
    	return wand;
    }
    
    public void restoreInventory(Player p){
    	if (settingArea.containsKey(p.getName())){
    		p.getInventory().setContents(settingArea.get(p.getName()));
    		p.getInventory().setArmorContents(armour.get(p.getName()));
    		settingArea.remove(p.getName());
    		armour.remove(p.getName());
    	}
    }
    
    public void restoreAllInventories(){
    	for (Player p : getServer().getOnlinePlayers())
    		restoreInventory(p);
    }
    
    public Set<Block> getBlocksAround(Location l){
		Set<Block> bs = new HashSet<Block>();
		Block lb = l.getBlock();
		if (lb.getRelative(BlockFace.NORTH).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.NORTH));
		if (lb.getRelative(BlockFace.NORTH_EAST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.NORTH_EAST));
		if (lb.getRelative(BlockFace.NORTH_WEST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.NORTH_WEST));
		if (lb.getRelative(BlockFace.EAST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.EAST));
		if (lb.getRelative(BlockFace.SOUTH).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.SOUTH));
		if (lb.getRelative(BlockFace.SOUTH_EAST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.SOUTH_EAST));
		if (lb.getRelative(BlockFace.SOUTH_WEST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.SOUTH_WEST));
		if (lb.getRelative(BlockFace.WEST).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.WEST));
		if (lb.getRelative(BlockFace.SELF).getType() != Material.AIR)
			bs.add(lb.getRelative(BlockFace.SELF));
		return bs;
	}
}
