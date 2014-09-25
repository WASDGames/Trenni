package me.darkwiiplayer.trenni;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Trenni extends JavaPlugin implements Listener {
	
	public final Logger logger = Logger.getLogger("minecraft");
	private static Trenni instance;
	
	public static Trenni getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		if (!CoinType.coinExists("test")) {
			new CoinType("test", UUID.randomUUID(), Material.GOLD_INGOT, 0.1);
		}
		
		logger.log(Level.INFO, "Trenni has been loaded! :)");
		CoinType.reload();

		getServer().getPluginManager().registerEvents(this, this);
		instance = this;
	}
	
	@Override
	public void onDisable() {
		logger.log(Level.INFO, "Trenni has been unloaded!");
		CoinType.dump();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		
		//TODO: Debugging experiments to be removed before releasing!
		Player player = event.getPlayer();
		player.sendMessage("-------------\nHello :)\nHere are your stats:\n" + PlayerInfo.newPlayerFromID(player.getUniqueId()).toString() + "\n-------------");
		PlayerInfo.newPlayerFromID(player.getUniqueId()).save();
	}	
}