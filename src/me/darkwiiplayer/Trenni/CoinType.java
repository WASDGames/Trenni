package me.darkwiiplayer.trenni;

/* Documentation:
 * addList() Adds the CoinType to the list and returnes true if another coin was overwritten and false otherwise.
 * removeList() Tries to remove the coin type from the list and returnes true it it was removed and false if it wasn't in the list in the first place.
 * loadMap(HashMap) Removes the coin frmo the list, tries to load the data from the given hashmap and adds it to the list if successfull.
 * toMap() creates a HashMap that represents the coin
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class CoinType {
	public static HashMap<String, CoinType> coinMap = new HashMap<String, CoinType>(); // = new HashMap<String, CoinType>(); //Where all the other coins are saved
	private static File saveFile = new File("plugins/trenni/coins.yml");
	//FIXME: Change back to private!!!
	
	private String name;
	private UUID ownerID;
	private ArrayList<String> licenses;
	private Material material;
	private int amount;
	

	public CoinType() {
		resetValues();
	}
	
	public CoinType(HashMap map) {
		loadMap(map);
	}
	
	public CoinType(String _name, UUID _ownerID, Material _material, int _amount) {
		this();
		name = _name.toLowerCase();
		ownerID = _ownerID;
		material = _material;
		if (_amount > 0) {
			amount = _amount;
		} else {
			amount = 1;
		}
		licenses = new ArrayList<String>();
		addList();
	}
	
	//========== End of Constructors =======================
	
	private void resetValues() {
		removeList();
		ownerID = null;
		name = "";
		licenses = new ArrayList();
		material = Material.AIR;
		amount = 1;
	}
	
	private boolean isCoinMap(HashMap map) {
		if (! (map.get("name") instanceof String)) { return false; }
		if (! (map.get("material") instanceof String)) { return false; }
		if (! (map.get("amount") instanceof Integer)) { return false; }
		if (! (map.get("ownerID") instanceof String)) { return false; }
		if (UUID.fromString((String)map.get("ownerID")) == null) { return false; }
		ArrayList<Object> _licenses;
		if (map.get("licenses") instanceof ArrayList) {
			_licenses = (ArrayList)map.get("licenses");
			for (Object i : _licenses) {
				if (! ((i instanceof String) & (UUID.fromString((String) i) != null))) { return false; }
			}
		} else { return false; }
		
		return true;
	}
	
	public Boolean removeList() { //Returns True if the coin was removed and false if the coin wasn't in the list
		if (coinMap.remove(name) != null) {
			return false;
		}
		return true;
	}
	
	public Boolean addList() { //Returns true if a coin was overwritten
		if (coinMap.put(name.toLowerCase(), this) == null) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadMap(HashMap<Object, Object> map) { //Load coin from map and register to list if valid
		removeList();
		
		//check all the values for correct types:
		if (licenses == null) {
			licenses = new ArrayList<String>();
		}
		if (isCoinMap(map)) {
			name = (String)map.get("name").toString().toLowerCase();
			material = Material.getMaterial((String)map.get("material"));
			amount = (int)map.get("amount");
			ownerID = UUID.fromString((String)map.get("ownerID"));
			licenses = (ArrayList<String>)map.get("licenses");
		} else {
			resetValues();
			return false;
		}
		
		addList();
		return true;
	}
	
	public HashMap<String, Object> toMap() { //Saves a coin to a HashMap
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("name", name);
		map.put("material", material.name());
		map.put("amount", amount);
		map.put("ownerID", ownerID.toString());
		map.put("licenses", licenses);
		
		return map;
	}
	
	public static boolean reload() { //Reloads ALL coins from a file
		coinMap = new HashMap<String, CoinType>();
		FileInputStream FStream;
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
		saveFile.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			saveFile.createNewFile();
		} catch (IOException e) { //TODO: Change error message!
			Trenni.getInstance().logger.log(Level.SEVERE, "UUID/Join list file could not be created - Java error: " + e.getMessage());
			return false;
		}
		
		try {
			FStream  = new FileInputStream(saveFile);
			for (Object data : yaml.loadAll(FStream)) {
				new CoinType((HashMap)data);
			}
			FStream.close();
			return true;
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "Could not load coin file - File not found!");
			return false;
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "Error loading coins: " + e.getMessage());
			return false;
		}
	}

	public static boolean dump() { //Dumps ALL coins into a file
		FileWriter FWriter;
		DumperOptions options = new DumperOptions();
	    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		
		saveFile.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			saveFile.createNewFile();
		} catch (IOException e) { //TODO: Change error message!
			Trenni.getInstance().logger.log(Level.SEVERE, "UUID/Join list file could not be created - Java error: " + e.getMessage());
			return false;
		}
		
		ArrayList<HashMap> mapList = new ArrayList<HashMap>();
		for (CoinType coin : coinMap.values()) {
			mapList.add(coin.toMap());
		}
		
		try {
			FWriter = new FileWriter(saveFile);
			
			yaml.dumpAll(mapList.iterator(), FWriter);
			
			FWriter.close();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "Error loading coins: " + e.getMessage());
			return false;
		}
		
		return false;
	}

	public boolean delete() {
		//TODO: Implement this
		return false;
	}
	
	public static boolean coinExists(String name) {
		for (CoinType type : coinMap.values()) {
			if (type.name.equalsIgnoreCase(name)) {
				return true;
			}
		}		
		return false;
	}
	
	public boolean grantLicense(UUID id) {
		licenses.add(id.toString());		
		return true;
	}
	
	public boolean revokeLicense(UUID id) { //Tries to remove a licesnse from the list and returns true if it had to be removed and false if it didn't exist anyway
		if (licenses.contains(id.toString())) {
			licenses.remove(id.toString());
			return true;
		} else {
			return false;
		}
	}
	
	public int totalAmount() {
		ArrayList<PlayerInfo> list = PlayerInfo.loadAllPlayers();
		int amount = 0;
		
		for (PlayerInfo info : list) {
			amount  += info.amountOf(name);
		}
		
		return amount;
	}
	
	public String formattedInfo() {
		return "Coin information for: " + name 
				+ "\nOwner: " + Trenni.getInstance().getServer().getOfflinePlayer(ownerID).getName()
				+ "\nOwner ID: " + ownerID
				+ "\nMaterial: " + 1.0/(float)amount + " of " + material + " in each coin"
				+ "\nTotal amount: " + totalAmount();
	}
	
	public static CoinType getCoin(String name) {
		return coinMap.get(name.toLowerCase());
	}
	
	private static class CoinIterator implements Iterator<Object>{
		int i = -1;
		
		@Override
		public boolean hasNext() {
			return i < coinMap.size()-1;
		}

		@Override
		public Object next() {
			i+=1;
			return coinMap.values().toArray()[i];
		}
		
		public void remove() {
			if (i >= 1) {
				coinMap.remove(coinMap.keySet().toArray()[i]);
			}
		}
	}
	
	public static CoinIterator iterator() {
		return new CoinIterator();
	}
	
	public Material getMaterial() {
		return material;
	}

	public String getName() {
		return name;
	}
	
	public int howMany(int number) { //How many coins do I get with <number> amount of <material>?
		return amount * number;
	}
	
	public boolean hasLicense(Player player) {
		if (ownerID.equals(player.getUniqueId()) | licenses.contains(player.getUniqueId())) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isOwner(Player player) {
		if (ownerID.equals(player.getUniqueId())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int ownedCoinsCount(Player player) {
		int count = 0;
		for (CoinType coin : coinMap.values()) {
			if (coin.isOwner(player)) {
				count += 1;
			}
		}
		return count;
	}
	
	public static HashMap<String, CoinType> ownedCoins(Player player) {
		HashMap<String, CoinType> map = new HashMap<String, CoinType>();
		for (CoinType coin : coinMap.values()) {
			if (coin.isOwner(player)) {
				map.put(coin.name, coin);
			}
		}
		return map;
	}
	
	public static int managedCoinsCount(Player player) {
		int count = 0;
		for (CoinType coin : coinMap.values()) {
			if (coin.hasLicense(player)) {
				count += 1;
			}
		}
		return count;
	}
	
	public static HashMap<String, CoinType> managedCoins(Player player) {
		HashMap<String, CoinType> map = new HashMap<String, CoinType>();
		for (CoinType coin : coinMap.values()) {
			if (coin.hasLicense(player)) {
				map.put(coin.name, coin);
			}
		}
		return map;
	}
	
	public void grant(Player player) {
		licenses.add(player.getUniqueId().toString());
	}
	
	public void revoke(Player player) {
		licenses.remove(player.getUniqueId().toString());
	}
}
