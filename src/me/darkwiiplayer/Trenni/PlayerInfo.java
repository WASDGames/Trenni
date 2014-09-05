package me.darkwiiplayer.Trenni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class PlayerInfo {
	public UUID playerID;
	public HashMap<String, Integer> purse = new HashMap<String, Integer>();
	public HashMap<Integer, Integer> refundMaterials = new HashMap<Integer, Integer>();
	
	public PlayerInfo playerFromID(UUID id) {
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
					if (UUID.fromString((String)((HashMap)data).get(playerID)).equals(id)) {
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
}