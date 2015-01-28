package me.stashcat.PlugProtect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.stashcat.PlugProtect.Listeners.AreaListener;
import me.stashcat.PlugProtect.Metrics.Metrics;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
	
	private String prefix;
	public AreaListener AreaListener;
	public Map<String, ItemStack[]> settingArea = new HashMap<String, ItemStack[]>();
	public Map <String, ItemStack[]> armour = new HashMap <String, ItemStack[]>();
	public Map<String, String> settingWelcome = new HashMap<String, String>();
	public Map<String, String> settingFarewell = new HashMap<String, String>();
	public Map<String, String> modifying = new HashMap<String, String>();
	public Map<String, Location> pos1 = new HashMap<String, Location>();
	public Map<String, Location> pos2 = new HashMap<String, Location>();
	FileConfiguration data = null;
	File dataFile = null;
	ItemStack wand;
	public Areas Areas;
	public Metrics Metrics;
	
	public void onEnable(){
		saveDefaultConfig();
		saveConfig();
		saveDefaultCConfig();
		saveCConfig();
		initWand();
		getCommand("pp").setExecutor(new Commands(this));
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
		AreaListener = new AreaListener(this);
		prefix = "[" + ChatColor.GREEN + ChatColor.BOLD + getDescription().getName() + ChatColor.RESET + "] ";
		getLogger().info(getDescription().getFullName() + " Enabled!");
	}
	
	public void onDisable(){
		getLogger().info(getDescription().getFullName() + " Disabled!");
		restoreAllInventories();
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
            @SuppressWarnings("deprecation")
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
