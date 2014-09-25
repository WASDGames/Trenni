package me.darkwiiplayer.trenni;
//TODO: Actually check if the coin exists before doing anything

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class PlayerInfo {
	private UUID playerID;
	private HashMap<String, Integer> purse = new HashMap<String, Integer>();
	private HashMap<String, Integer> refundMaterials = new HashMap<String, Integer>();
	
	private Server server = Trenni.getInstance().getServer();
	
	public PlayerInfo() {
		
	}
	
	public PlayerInfo(PlayerInfo source) { //Makes a copy of an object
		playerID = UUID.fromString(source.playerID.toString());
		purse = new HashMap(source.purse);
		refundMaterials = new HashMap(source.refundMaterials);
	}
	
	public static PlayerInfo playerFromID(UUID id) {
		PlayerInfo info = new PlayerInfo();
		File file = new File("plugins/trenni/placeholder2.yml");
		
		file.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Trenni.getInstance().logger.log(Level.SEVERE,
					"UUID/Join list file could not be created - Java error: "
							+ e.getMessage());
			return null;
		}
		
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		FileInputStream IStream;
		
		try {
			IStream = new FileInputStream(file);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					HashMap map = (HashMap)data;
					if (UUID.fromString((String)((HashMap)data).get("playerID")).equals(id)) {
						if (map.get("purse") == null) {
							map.put("purse", new ArrayList());
						}
						if (map.get("refundMaterials") == null) {
							map.put("refundMaterials", new ArrayList());
						}
						if ((map.get("purse") instanceof ArrayList)
								& (map.get("refundMaterials") instanceof ArrayList)) { //Check if everything is as it shoud
							info.playerID = id;
							info.purse = (HashMap)map.get("purse");
							info.refundMaterials = (HashMap)map.get("refundMaterials");
							
							return info;
						}
					}
				}
			}
			
			IStream.close();			
			return null;
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "Could not load user with ID " + id.toString() + " player file not found!");
			return null;
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "Unknown error while opening user with ID: " + id.toString());
			return null;
		}
	}
	
	public static PlayerInfo newPlayerFromID(UUID id) {
		PlayerInfo object = playerFromID(id);
		if (object == null) {
			PlayerInfo info = new PlayerInfo();
			info.playerID = id;
			info.purse.put("test", 350);
			return info;
		} else {
			return object;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean save() {
		
		for (Entry<String, Integer> entry : purse.entrySet()) {
			if (entry.getValue() <= 0) {
				purse.remove(entry.getKey());
			}
		}
		
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		FileWriter FWriter;
		FileInputStream IStream;
		File file = new File("plugins/trenni/placeholder2.yml");
		
		file.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			file.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE,
					"UUID/Join list file could not be created - Java error: "
							+ e.getMessage());
			return false;
		}
		
		HashMap<String, HashMap> objectMap = new HashMap<String, HashMap>();
		HashMap<Object, Object> playerMap = new HashMap<Object, Object>();
			playerMap.put("playerID", playerID.toString());
			playerMap.put("purse", purse);
			playerMap.put("refundMaterials", refundMaterials);
		objectMap.put(playerID.toString(), objectMap);
		
		try {
			IStream = new FileInputStream(file);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					if (((HashMap)data).get("playerID") instanceof String) {
						objectMap.put((String)((HashMap)data).get("playerID"), (HashMap)data);
					}
				}
			}			
			IStream.close();			
			objectMap.put(playerID.toString(), playerMap);
			
			FWriter = new FileWriter(file);
			
			yaml.dumpAll(objectMap.values().iterator(), FWriter);
			FWriter.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}		
	}
	
	public boolean pay(UUID id, Integer amount, String coin) {
		if (purse.get(coin) >= amount) {
			PlayerInfo other = newPlayerFromID(id);
			other.purse.put(coin, other.purse.get(coin) + amount);
			purse.put(coin, purse.get(coin) - amount);
			return true;
		} else {
			return false;
		}
	}

	public boolean remove(String coin, int amount) {
		if (!CoinType.coinExists(coin)) {
			return false;
		}
		
		if (amountOf(coin) >= amount) {
			setAmountOf(coin, amountOf(coin) - amount);
			return true;
		}
		return false;
	}
	
	public int xRemove(String coin, int amount) {
		if (!CoinType.coinExists(coin)) {
			return 0;
		}
		
		if (amountOf(coin) >= amount) {
			setAmountOf(coin, amountOf(coin) - amount);
			return amount;
		} else {
			int dif = amount - amountOf(coin);
			setAmountOf(coin, 0);
			return dif;
		}
	}
	
	public boolean setAmountOf(String coin, int value) {
		if (!CoinType.coinExists(coin)) {
			return false;
		} else {
			if (value > 0) {
				purse.put(coin.toLowerCase(), value);
			} else {
				purse.remove(coin.toLowerCase());
			}
			return true;
		}
	}
	
	public Integer amountOf(String name) {
		if (purse.get(name) instanceof Integer) {
			return purse.get(name.toLowerCase());
		} else {
			return 0;
		}
	}
	
	public boolean refund() {
		PlayerInventory inventory = server.getPlayer(playerID).getInventory();
		for (Entry<String, Integer> entry : refundMaterials.entrySet()) {
			inventory.addItem(new ItemStack(Material.getMaterial(entry.getKey()), entry.getValue()));
		}
		return true;
	}

	public UUID getID() {
		return playerID; //UUID.fromString(playerID.toString());		
	}

	public String toString() {
		return "PlayerID: " + playerID.toString()
				+ "\npurse: " + purse.toString()
				+ "\nrefundMaterials: " + refundMaterials.toString();
	}

	public HashMap<String, Integer> getPurse() {
		return new HashMap<String, Integer>(purse);
	}	
	
	public HashMap<String, Integer> getRefunds() {
		return new HashMap<String, Integer>(refundMaterials);
	}
	
}