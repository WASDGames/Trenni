package me.darkwiiplayer.Trenni;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class TrenniPlugin extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("minecraft");
	private static TrenniPlugin instance;
	public static TrenniPlugin getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		logger.log(Level.INFO, "Trenni has been loaded! :)");
		CoinType.reload();
	}
	
	@Override
	public void onDisable() {
		logger.log(Level.INFO, "Trenni has been unloaded!");
		CoinType.dump();
	}
}
