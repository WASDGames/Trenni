package me.darkwiiplayer.trenni;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrenniCommandExecutor implements CommandExecutor{
	Trenni plugin;

	public TrenniCommandExecutor(Trenni trenniPlugin) {
		plugin = trenniPlugin;
	}
	
	public int removeItem(Inventory inventory, Material material,  int quantity) { //Removes <quantity> items from the inventory and returns the rest
        int rest = quantity;
        for( int i = 0 ; i < inventory.getSize() ; i++ ){
            ItemStack stack = inventory.getItem(i); 
            if( stack == null || stack.getData().getItemType() != material ) {
                continue;            	
            }
            if( rest >= stack.getAmount() ){
                rest -= stack.getAmount();
                inventory.clear(i);
            } else if( rest>0 ){
                    stack.setAmount(stack.getAmount()-rest);
                    rest = 0;
            } else {
                break;
            }
        }
        return rest;
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command _command, String label, String[] args) {
		MaterialList materialList = new MaterialList();
		materialList.loadBlacklist();
		materialList.loadWhitelist();
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
						if (arguments.size() < 3) {
							sender.sendMessage("[Trenni] Too few arguments!");
							sender.sendMessage("[TRENNI] /Trenni pay <player> <amount> <currency>");
							return true;
						}
						Player other = plugin.getServer().getPlayer(arguments.get(0));
						
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
				
				//==================================================================================================
				//=== COIN COMMANDS ================================================================================
				//==================================================================================================
				
				if (command.equals("list")) {
					if (arguments.size() == 0) {
						String str = "";
						for (Object coin : new CoinIterable()) {
							str += ((CoinType)coin).getName() + ", ";
						}					
						sender.sendMessage("[TRENNI] List of all existing coins:");
						sender.sendMessage(str);
						
						return true;
					} else {
						if (arguments.get(0).equals("owned")) {
							if (sender instanceof Player) {
								String str = "";
								for (CoinType coin : CoinType.ownedCoins((Player)sender).values()) {
									str += coin.getName() + ", ";
								}
								sender.sendMessage(str);
							} else {
								sender.sendMessage("You are the console, you own no coins.");
							}
							return true;
						}
						if (arguments.get(0).equals("managed")) {
							if (sender instanceof Player) {
								String str = "";
								for (CoinType coin : CoinType.managedCoins((Player)sender).values()) {
									str += coin.getName() + ", ";
								}
								sender.sendMessage(str);
							} else {
								sender.sendMessage("You are the console, you manage no coins.");
							}
							return true;
						}
						if (arguments.get(0).equals("all")) {
							String str = "";
							for (Object coin : new CoinIterable()) {
								str += ((CoinType)coin).getName() + ", ";
							}					
							sender.sendMessage("[TRENNI] List of all existing coins:");
							sender.sendMessage(str);
							
							return true;
						}
						
						sender.sendMessage("[TRENNI] Invalid command syntax! /trenni list [owned|all|managed]");
						return true;
					}
				}
				
				if (command.equals("coin"))
				{
					if (arguments.size() == 0) {
						//TODO: Better error message
						sender.sendMessage("[TRENNI] Error, command not supported. Try /trenni coin help");
						return true;
					}
					
					if (!CoinType.coinExists(arguments.get(0))) {
						//TODO: Better error message
						sender.sendMessage("[TRENNI] There is no coin named " +  arguments.get(0));
						return true;
					}
					CoinType type = CoinType.getCoin(arguments.get(0));
					
					if (arguments.size() == 1) {
						sender.sendMessage(type.formattedInfo());
						return true;
					} else { //if !(arguments.size() == 0)
						
						//trenni coin all command:
						
						if (arguments.get(0).equals("all")) {
							String str = "";
							for (Object coin : new CoinIterable()) {
								str += ((CoinType)coin).getName() + ", ";
							}					
							sender.sendMessage("[TRENNI] List of all existing coins:");
							sender.sendMessage(str);
							
							return true;
						}
						
						//trenni coin <coin> commands:
						
						CoinType coin = CoinType.getCoin(arguments.get(0));
						arguments.remove(0);
						command = arguments.get(0);
						arguments.remove(0);
						
						if (command.equals("create")) {
							if (arguments.size() < 1) {
								sender.sendMessage("[TRENNI] /trenni coin <coin> create <amount>");
								return true;
							}
							
							if (!(sender instanceof Player)) {
								sender.sendMessage("You are the console! You have no items, how would you create coins?");
								return true;
							}
							Player player = (Player)sender;
							
							if (!(coin.hasLicense(player) | coin.isOwner(player) | player.hasPermission("trenni.admin.create"))) {
								player.sendMessage("[TRENNI] You need a license to create that type of coin.");
								return true;
							}
							
							try {
								if (Integer.parseInt(arguments.get(0)) <= 0) {
									sender.sendMessage("You cannot produce that amount of coins lol");
									return true;
								}
							} catch (NumberFormatException e) {
								return true;
							}
							
							
							if (player.getInventory().contains(coin.getMaterial(), Integer.parseInt(arguments.get(0)))) {
								PlayerInfo info = PlayerInfo.newPlayerFromID(player.getUniqueId());
								
								info.add(coin.getName(), coin.howMany(Integer.parseInt(arguments.get(0))));
								removeItem(player.getInventory(), coin.getMaterial(), Integer.parseInt(arguments.get(0)));
								info.save();
								
								sender.sendMessage("[TRENNI] You have created " + coin.howMany(Integer.parseInt(arguments.get(0))) + " " + coin.getName());
								
								return true;
							} else {
								sender.sendMessage("[TRENNI] You don't have enough " + coin.getMaterial().name() + " to do that!");
								
								return true;								
							}
						}
						//end of create command
						
						if (command.equals("grant")) {
							if ( !(sender instanceof Player) ) {
								sender.sendMessage("[TRENNI] You are the console, you own no coins.");
								return true;
							}
							
							if (arguments.size() == 0) {
								sender.sendMessage("[TRENNI] You need to specify a player.");
								return true;
							}
							
							Player player = Trenni.getInstance().getServer().getPlayer(arguments.get(0));
							if (player == null) {
								sender.sendMessage("[TRENNI] Player " + arguments.get(0) + " not found.");
								return true;
							}
							
							coin.grant(player);
							sender.sendMessage("[TRENNI] License granted");
							return true;
						}
						//end of grant command
						
						if (command.equals("revoke")) {
							if ( !(sender instanceof Player) ) {
								sender.sendMessage("[TRENNI] You are the console, you own no coins.");
								return true;
							}
							
							if (arguments.size() == 0) {
								sender.sendMessage("[TRENNI] You need to specify a player.");
								return true;
							}
							
							Player player = Trenni.getInstance().getServer().getPlayer(arguments.get(0));
							if (player == null) {
								sender.sendMessage("[TRENNI] Player " + arguments.get(0) + " not found.");
								return true;
							}
							
							coin.revoke(player);
						}						
						//end of revoke command
						
						if (command.equals("drop")) {
							if ( !(sender instanceof Player) ) {
								sender.sendMessage("[TRENNI] You are the console. You cannot drop any coins.");
								return true;
							}
							
							if (arguments.size() == 0) {
								sender.sendMessage("[TRENNI] Too few arguments! /trenni coin <coin> drop <amount>");
							}
							
							try {
								if (Integer.parseInt(arguments.get(0)) <= 0) {
									sender.sendMessage("[TRENNI] You cannot drop negative amounts of coins lol");
									return true;
								}
							} catch (NumberFormatException e) {
								sender.sendMessage("[TRENNI] Size has to be an amount! /trenni coin <coin> drop <amount>");
								return true;
							}
							Integer amount = Integer.parseInt(arguments.get(0));
							Player player = (Player)sender;
							PlayerInfo info = PlayerInfo.newPlayerFromID(player.getUniqueId());
							player.sendMessage("You have dropped " + info.xRemove(coin.getName(), amount) + " " + coin.getName());
							info.save();
							return true;
						}
						
						
						sender.sendMessage("[TRENNI] invalid command syntax!");
						return true;
					}
				}
				
				//==================================================================================================
				//=== COIN COMMANDS END ============================================================================
				//==================================================================================================
				
				if (command.equals("create")) {
					
					if (!(sender instanceof Player)) {
						sender.sendMessage("You are the console, you cannot own any coins!");
						return true;
					}
					Player player = (Player)sender;
					
					if (arguments.size() < 3) {
						sender.sendMessage("[TRENNI] Not enough arguments.");
						sender.sendMessage("[TRENNI] /Trenni create <name> <material> <amount>");
						return true;
					}
					
					if (CoinType.coinExists(arguments.get(0))) {
						sender.sendMessage("[TRENNI] The coin " + arguments.get(0) + " does already exist!");
						return true;
					}
					
					if (Material.getMaterial(arguments.get(1).toUpperCase()) == null) {
						sender.sendMessage("[TRENNI] Invalid material: " + arguments.get(1));
						sender.sendMessage("[TRENNI] pssst! The item in your hand is " + ((Player)sender).getInventory().getItemInHand().getType().name() );
						return true;
					}
					try {
						if (!(Integer.parseInt(arguments.get(2)) > 0)) {
							//TODO: Change this error message!
							sender.sendMessage("[Trenni] Invalid argument, amount has to be greater than 0!");
							sender.sendMessage("[TRENNI] /Trenni pay <player> <amount> <currency>");
							return true;
						}
					} catch (NumberFormatException e) {
						sender.sendMessage("[Trenni] Invalid argument, amount has to be an integer!");
						return true;
					}
					
					if ( !(materialList.isAllowed(Material.getMaterial(arguments.get(1).toUpperCase())) | player.hasPermission("trenni.ignoreblacklist")) ) {
						player.sendMessage("[TRENNI] that item (" + Material.getMaterial(arguments.get(1).toUpperCase()).name() + ") is blacklisted");
						return true;
					}
					
					CoinType coin = (new CoinType(arguments.get(0), ((Player)sender).getUniqueId(), Material.getMaterial(arguments.get(1).toUpperCase()), Integer.parseInt(arguments.get(2)))); //Integer.parseInt(arguments.get(2))));
					coin.addList();
					
					CoinType.dump();
					sender.sendMessage("Coin successfully created :)");
					return true;
				}
			//End interpreting commands
			}			
		}		
		sender.sendMessage("[TRENNI] Invalid command syntax, try /trenni help");
		return true;
	}

}
