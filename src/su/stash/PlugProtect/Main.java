package su.stash.PlugProtect;

import java.util.HashSet;
import java.util.Set;

import me.stashcat.enCore.ClassBase;

import org.bukkit.plugin.java.JavaPlugin;

import su.stash.PlugProtect.listeners.MainListener;

public class Main extends JavaPlugin implements ClassBase {
	MainListener mlisten;
	Set<String> settingRegions = new HashSet<String>(); //String must be a playername
	
	public void onEnable(){
		getCommand("plugprotect").setExecutor(new Commands(this));
		mlisten = new MainListener(this);
		log("Plugin started.");
	}
	
	public void onDisable(){
		log("Plugin stopped.");
	}
}
