package me.darkwiiplayer.Trenni;

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
	
	private Server server = TrenniPlugin.getInstance().getServer();
	
	public static PlayerInfo playerFromID(UUID id) {
		PlayerInfo info = new PlayerInfo();
		File file = new File("placeholder2.txt");
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
			TrenniPlugin.getInstance().logger.log(Level.SEVERE, "Could not load user with ID " + id.toString() + " player file not found!");
			return null;
		} catch (IOException e) {
			TrenniPlugin.getInstance().logger.log(Level.SEVERE, "Unknown error while opening user with ID: " + id.toString());
			return null;
		}
	}
	
	public static PlayerInfo newPlayerFromID(UUID id) {
		PlayerInfo object = playerFromID(id);
		if (object == null) {
			return new PlayerInfo();
		} else {
			return object;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void save() {
		//TODO: Implement saving!
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		FileWriter FWriter;
		FileInputStream IStream;
		File file = new File("placeholder2.txt");
		HashMap<String, HashMap> objectMap = new HashMap<String, HashMap>();
		HashMap<Object, Object> playerMap = new HashMap<Object, Object>();
			playerMap.put("playerID", playerID);
			playerMap.put("purse", purse);
			playerMap.put("refundMaterials", refundMaterials);
		
		try {
			IStream = new FileInputStream(file);
			
			for (Object data : yaml.loadAll(IStream)) {
				if (data instanceof HashMap) {
					if (((HashMap)data).get("playerID") instanceof String) {
						objectMap.put((String)((HashMap)data).get("playerID"), (HashMap)data);
					}
				}
			}
			
			objectMap.put(playerID.toString(), playerMap);
			
			yaml.dumpAll(objectMap.values().iterator());
			
			IStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		
		/* Load All
		 * Overwrite self <- This is probably the trickiest part =/
		 * Save All
		 */
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

	public boolean refund() {
		PlayerInventory inventory = server.getPlayer(playerID).getInventory();
		for (Entry<String, Integer> entry : refundMaterials.entrySet()) {
			inventory.addItem(new ItemStack(Material.getMaterial(entry.getKey()), entry.getValue()));
		}
		return true;
	}
	
}