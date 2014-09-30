package me.darkwiiplayer.trenni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import org.yaml.snakeyaml.Yaml;

public class Options {
	public static String filename = "plugins/trenni/options.yml";
	
	public static Object load(String option, Class optionClass) {
		File file = new File(filename);
		file.getParentFile().mkdirs();
		try { // Create the file of it doesn't exist yet
			file.createNewFile();
		} catch (IOException e) { //TODO: Change error message!
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Could not create options file:  " + e.getMessage());
			return false;
		}
		Yaml yaml = new Yaml();
		FileInputStream IStream;
		
		try {
			IStream = new FileInputStream(file);
			
			Object input = yaml.load(IStream);
			if ( !(input instanceof HashMap) ) {				
				IStream.close();
				return null;
			}
			
			if ( ((HashMap)input).get(option).getClass().equals(optionClass) ) {				
				IStream.close();
				return input;
			} else {				
				IStream.close();
				return null;
			}
			
		} catch (FileNotFoundException e) {
			Trenni.getInstance().logger.log(Level.SEVERE, "[TRENNI] Error opening options file: file not found");
			Trenni.getInstance().logger.log(Level.WARNING, "[TRENNI] This shouldn't ever happen, please contact the author!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
