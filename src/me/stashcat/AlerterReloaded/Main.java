package me.stashcat.AlerterReloaded;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
	
	private String prefix;
	
	public void onEnable(){
		prefix = "[" + ChatColor.GREEN + ChatColor.BOLD + getDescription().getName() + ChatColor.RESET + "] ";
	}
	
	public void onDisable(){
		
	}
	
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("al") || cmd.getName().equalsIgnoreCase("alerter")){
			if (args.length == 0){
				sendMsg(s, true, "&2" + getDescription().getFullName() + "&r by " + getDescription().getAuthors());
				sendMsg(s, true, "Use &a/al help&r to see all commands.");
				sendMsg(s, true, "In memory of the original Alerter plugin by &apatrick_pk91&r");
			} else if (args.length == 1 && args[0].equalsIgnoreCase("help") && s.hasPermission("alerter")){
				if (s.hasPermission("alerter")) sendMsg(s, true, "&/al &2help&r  - Displays help about all of the commands");
				if (s.hasPermission("alerter.region")) sendMsg(s, true, "&/al &2ARG&r  - DESC");
				//if (s.hasPermission("alerter.PERM")) sendMsg(s, true, "&/al &2ARG&r  - DESC");
			}
		}
		return false;
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
    
    public void addListElement(Plugin p, String key, String... element) {
        List<String> list = p.getConfig().getStringList(key);
        list.addAll(Arrays.asList(element));
        p.getConfig().set(key, list);
        p.saveConfig();
    }
}
