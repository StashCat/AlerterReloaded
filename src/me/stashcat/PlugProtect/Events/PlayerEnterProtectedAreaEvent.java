package me.stashcat.PlugProtect.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerEnterProtectedAreaEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Player p;
	private String area;
	private String msg;
	
	public PlayerEnterProtectedAreaEvent(Player p, String area, String msg){
		this.p = p;
		this.area = area;
		this.msg = msg;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public String getAreaId(){
		return area;
	}
	
	public String getMessage(){
		return msg;
	}
}
