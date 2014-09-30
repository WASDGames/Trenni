package me.darkwiiplayer.trenni;
//TODO: Actually check if the coin exists before doing anything

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

public class PlayerInfo implements Iterable<PurseEntry> {
	private static File saveFile = new File("plugins/trenni/players.yml");
	private UUID playerID;
	private HashMap<String, Integer> purse = new HashMap<String, Integer>();
	private HashMap<String, Float> refundMaterials = new HashMap<String, Float>();
		
	private static void info(String msg) {
		Trenni.getInstance().logger.log(Level.INFO, msg);
	}
		
	private Server server = Trenni.getInstance().getServer();
	
	public PlayerInfo() {
		
	}
	
	public PlayerInfo(PlayerInfo source) { //Makes a copy of an object
		playerID = UUID.fromString(source.playerID.toString());
		purse = new HashMap(source.purse);
		refundMaterials = new HashMap(source.refundMaterials);
	}
	
	private void removeUnexistentCoins() {
		Iterator<PurseEntry> i = iterator();
		while (i.hasNext()) {
			if (!CoinType.coinExists(i.next().getName())) {
				i.remove();
			}
		}
	}
	
	public static PlayerInfo playerFromID(UUID id) {
		PlayerInfo info = new PlayerInfo();
		
		saveFile.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			saveFile.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "UUID/Join list file could not be created - Java error: " + e.getMessage());
			return null;
		}
		//YAML stuff:
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
		//Read the File:
		FileInputStream IStream;
		try {
			IStream = new FileInputStream(saveFile);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					HashMap map = (HashMap)data;
					if (map.get("playerID") == null) {
						continue;
					}
					if (UUID.fromString((String)((HashMap)data).get("playerID")).equals(id)) {
						if (map.get("purse") == null) {
							map.put("purse", new HashMap<String, Integer>());
						}
						if (map.get("refundMaterials") == null) {
							map.put("refundMaterials", new HashMap<String, Integer>());
						}
						if ((map.get("purse") instanceof HashMap) & (map.get("refundMaterials") instanceof HashMap)) { //Check if everything is as it shoud
							info.playerID = id;
							info.purse = (HashMap)map.get("purse");
							info.refundMaterials = (HashMap)map.get("refundMaterials");

							IStream.close();
							info.removeUnexistentCoins();
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
			info.setAmountOf("test", 350);
			return info;
		} else {
			return object;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean save() {
		Iterator<Entry<String, Integer>> iterator = purse.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue() < 0) {
				iterator.remove();
			}
		}
		
		/*
		for (Entry<String, Integer> entry : purse.entrySet()) {
			if (entry.getValue() <= 0) {
				purse.remove(entry.getKey());
			}
		}
		*/
		
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		FileWriter FWriter;
		FileInputStream IStream;
		
		saveFile.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			saveFile.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "UUID/Join list file could not be created - Java error: " + e.getMessage());
			return false;
		}
		
		HashMap<String, HashMap> objectMap = new HashMap<String, HashMap>();
		HashMap<Object, Object> playerMap = new HashMap<Object, Object>();
			playerMap.put("playerID", playerID.toString());
			playerMap.put("purse", purse);
			playerMap.put("refundMaterials", refundMaterials);
			playerMap.put("playerName", Trenni.getInstance().getServer().getOfflinePlayer(playerID).getName());
		objectMap.put(playerID.toString(), objectMap);
		
		try {
			IStream = new FileInputStream(saveFile);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					if (((HashMap)data).get("playerID") instanceof String) {
						objectMap.put((String)((HashMap)data).get("playerID"), (HashMap)data);
					}
				}
			}			
			IStream.close();			
			objectMap.put(playerID.toString(), playerMap);
			
			FWriter = new FileWriter(saveFile);
			
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
		if (amountOf(coin) >= amount) {
			PlayerInfo other = newPlayerFromID(id);
			other.add(coin, amount);
			remove(coin, amount);
			other.save();
			save();
			return true;
		} else {
			return false;
		}
	}

	public boolean remove(String coin, int amount) { //tries to remove <amount> coins and returns true if successfull. Doesn't remove anything if not enough coins.
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
			int removed = amountOf(coin);
			setAmountOf(coin, 0);
			return removed;
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
		for (Entry<String, Float> entry : refundMaterials.entrySet()) {
			inventory.addItem(new ItemStack(Material.getMaterial(entry.getKey()), (int)Math.round(Math.floor(entry.getValue()))));
			refundMaterials.put(entry.getKey(), entry.getValue() - (int)Math.round(Math.floor(entry.getValue())));
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
	
	public HashMap<String, Float> getRefunds() {
		return new HashMap<String, Float>(refundMaterials);
	}

	public boolean add(String coin, int amount) {
		if (CoinType.coinExists(coin)) {
			if (amountOf(coin) > 0) {
				purse.put(coin, purse.get(coin) + amount);
			} else {
				purse.put(coin, amount);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private class PurseIterator implements Iterator<PurseEntry>{
		private int i = 0;
		
		@Override
		public boolean hasNext() {
			return i < purse.size();
		}

		@Override
		public PurseEntry next() {
			i+=1;
			return new PurseEntry((String)purse.keySet().toArray()[i-1], (int)purse.values().toArray()[i-1]);
		}
		
		public void remove() {
			purse.remove(purse.keySet().toArray()[i-1]);
		}
		
	}
	
	@Override
	public Iterator<PurseEntry> iterator() {
		// TODO Auto-generated method stub
		return new PurseIterator();
	}

	public static ArrayList<PlayerInfo> loadAllPlayers() {
		ArrayList<PlayerInfo> list = new ArrayList<PlayerInfo>();
		
		saveFile.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			saveFile.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "UUID/Join list file could not be created - Java error: " + e.getMessage());
			return null;
		}
		//YAML stuff:
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
		//Read the File:
		FileInputStream IStream;
		try {
			IStream = new FileInputStream(saveFile);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					HashMap map = (HashMap)data;
					if (map.get("purse") == null) {
						map.put("purse", new HashMap<String, Integer>());
					}
					if (map.get("refundMaterials") == null) {
						map.put("refundMaterials", new HashMap<String, Integer>());
					}
					if ((map.get("purse") instanceof HashMap) & (map.get("refundMaterials") instanceof HashMap)) { //Check if everything is as it shoud
						PlayerInfo info = new PlayerInfo();
						info.playerID = UUID.fromString(((HashMap)map).get("playerID").toString());
						info.purse = (HashMap)map.get("purse");
						info.refundMaterials = (HashMap)map.get("refundMaterials");

						list.add(info);
					}
				}
			}			
			IStream.close();		
			
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Error loading player list! Player file not found.");
			return null;
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Error loading player list!");
			return null;
		}
		
		return list;
	}
		
}