package me.darkwiiplayer.trenni;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrenniCommandExecutor implements CommandExecutor{
	Trenni plugin;

	public TrenniCommandExecutor(Trenni trenniPlugin) {
		plugin = trenniPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command _command, String label, String[] args) {
		ArrayList<String> arguments = new ArrayList(Arrays.asList(args));
		if (_command.getName().equalsIgnoreCase("trenni") & arguments.size() > 0) {
			if (arguments.get(0) instanceof String) {
				String command = arguments.get(0);
				arguments.remove(0);
				//Start interpreting commands
				
				if (command.equalsIgnoreCase("pay")) {
				int amount = 0;
					if (sender instanceof Player) {
						Player player = (Player)sender;
						Player other = plugin.getServer().getPlayer(arguments.get(0));
						if (arguments.size() < 3) {
							sender.sendMessage("[Trenni] Too few arguments!");
							sender.sendMessage("[TRENNI] /Trenni pay <player> <amount> <currency>");
							return true;
						}
						
						if (other == null) {
							sender.sendMessage("[Trenni] Player " + arguments.get(0) + " not found");
							return true;
						}
						try { //TODO: at some random point, remove this so that players can pay negative amounts to troll users.
							if (!(Integer.parseInt(arguments.get(1)) > 0)) {
								sender.sendMessage("[Trenni] Invalid argument, amount has to be greater than 0!");
								sender.sendMessage("[TRENNI] /Trenni pay <player> <amount> <currency>");
								return true;
							} else {
								amount = Integer.parseInt(arguments.get(1));
							}
						} catch (NumberFormatException e) {
							sender.sendMessage("[Trenni] Invalid argument, amount has to be an integer!");
							return true;
						}
							
						if (!CoinType.coinExists(arguments.get(2))) {
							sender.sendMessage("[TRENNI] There is no coin named " + arguments.get(2));
							return true;
						}
						
						if (player.getEyeLocation().distance(other.getEyeLocation()) > 3) {
							sender.sendMessage("[TRENNI] Too far Away!");
							return true;
						}
						
						if (PlayerInfo.newPlayerFromID(((Player) sender).getUniqueId()).pay(plugin.getServer().getPlayer(arguments.get(0)).getUniqueId(), amount, arguments.get(2)) == true) {
							sender.sendMessage("[TRENNI] Transaction successful.");
							plugin.getServer().getPlayer(arguments.get(0)).sendMessage("[TRENNI] " + sender.getName() + " has given you " + amount + " " + arguments.get(2));
						} else {
							sender.sendMessage("[TRENNI] You don't have enough coins for that!");
							return true;
						}
						
						return true;
					} else {
						sender.sendMessage("[TRENNI] You are the console! Use /Trenni give to give money to a player.");
						return true;
					} 
				}
				
				if (command.equals("info")) {
					if (sender instanceof Player) {
						sender.sendMessage("--------------------\nYou look into your purse and see:");
						for (PurseEntry entry : PlayerInfo.newPlayerFromID(((Player) sender).getUniqueId())) {
							sender.sendMessage(entry.getInfo());
						}
						return true;
					} else {
						sender.sendMessage("Shut up, you are the console!");
						return true;
					}
				}
				
				if (command.equals("coin"))
				{
					if (arguments.size() == 0) {
						//TODO: Better error message
						sender.sendMessage("Error");
						return true;
					}
					
					if (!CoinType.coinExists(arguments.get(0))) {
						//TODO: Better error message
						sender.sendMessage("coin does not exist");
						return true;
					}
					CoinType type = CoinType.getCoin(arguments.get(0));
					
					if (arguments.size() == 1) {
						sender.sendMessage(type.formattedInfo());
						return true;
					} else {
						return true;
					}
				}
			//End interpreting commands
			}			
		}		
		sender.sendMessage("[TRENNI] Invalid command syntax, try /trenni help");
		return true;
	}

}
