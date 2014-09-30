package me.darkwiiplayer.trenni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Material;
import org.yaml.snakeyaml.Yaml;

public class MaterialList {
	private static File fileBlacklist = new File("plugins/trenni/blacklist.yml");
	private static File fileWhitelist = new File("plugins/trenni/whitelist.yml");
	private ArrayList<String> blackList = new ArrayList<String>();
	private ArrayList<String> whiteList = new ArrayList<String>();

	public void loadBlacklist() {
		Yaml yaml = new Yaml();
		FileInputStream IStream;
		Object input;
		
		fileBlacklist.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			fileBlacklist.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Error creating item blacklist!");
			blackList = new ArrayList<String>();
			return ;
		}
		
		try {
			IStream = new FileInputStream(fileBlacklist);
			
			input = yaml.load(IStream);
			
			if (input instanceof ArrayList) {
				blackList = (ArrayList)input;
			}
			
			IStream.close();
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.WARNING, "[TRENNI] Could not open item blacklist!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}	

	public void loadWhitelist() {
		Yaml yaml = new Yaml();
		FileInputStream IStream;
		Object input;
		
		fileWhitelist.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			fileWhitelist.createNewFile();
		} catch (IOException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Error creating item whitelist!");
			whiteList = new ArrayList<String>();
			return ;
		}
		
		try {
			IStream = new FileInputStream(fileWhitelist);
			
			input = yaml.load(IStream);
			
			if (input instanceof ArrayList) {
				whiteList = (ArrayList)input;
			}
			
			IStream.close();			
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.WARNING, "[TRENNI] Could not open item whitelist!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}	

	public boolean isWhitelisted(Material material) {
		return (whiteList.size() == 0) | whiteList.contains(material.name()) | whiteList.contains(material.getId());
	}
	
	public boolean isBlacklisted(Material material) {
		return blackList.contains(material.name()) | blackList.contains(material.getId());
	}
	
	public boolean isAllowed(Material material) {
		return isWhitelisted(material) & !isBlacklisted(material);
	}
}
