package su.stash.PlugProtect;

import me.stashcat.enCore.ClassBase;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor, ClassBase {
	Main pl;
	
	public Commands(Main pl){
		this.pl = pl;
	}

	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (!canDo(s.getName())){
			sendMsg(s, "Please finish what you are currently doing first.");
			return true;
		}
		if (args[0].equalsIgnoreCase("help")){
			sendMsg(s, "  &aPlugProtect Help&r");
			sendMsg(s, "&a/" + label + " help&r - Displays this help menu.");
			if (s.hasPermission("plugprotect.manage")){
				sendMsg(s, "&a/" + label + " set&r - Starts setting a region.");
				sendMsg(s, "&a/" + label + " save [id]&r - Saves a set region with the specified ID.");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("set") && s.hasPermission("plugprotect.manage") && args.length == 1){
			
		} else {
			sendMsg(s, "&cInvalid arguments.");
			return true;
		}
		sendMsg(s, "Oops, something went wrong.");
		return false;
	}
	
	public void beginSetting(Player p){
		
	}
	
	public boolean canDo(String p){
		if (!pl.settingRegions.contains(p))
			return true;
		else
			return false;
	}
}
