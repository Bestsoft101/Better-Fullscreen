package b100.fullscreenfix.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ConfigUtil {
	
	public static void saveStringToFile(String string, File file) {
		OutputStream out = null;
		BufferedWriter bw = null;
		try {
			out = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write(string);
			bw.close();
		}catch (Exception e) {
			throw new RuntimeException("Writing file " + file.getAbsolutePath(), e);
		}finally {
			try {
				out.close();
			}catch (Exception e) {}
			try {
				bw.close();
			}catch (Exception e) {}
		}
	}
	
	public static void loadConfig(File configFile, ConfigParser configParser, char seperator) {
		if(!configFile.exists()) {
			return;
		}
		InputStream in = null;
		try {
			in = new FileInputStream(configFile);
			
			loadConfig(in, configParser, seperator);
		}catch (Exception e) {
			throw new RuntimeException("Loading config file: " + configFile.getAbsolutePath(), e);
		}finally {
			try {
				in.close();
			}catch (Exception e) {}
		}
	}
	
	public static void loadConfig(InputStream in, ConfigParser configParser, char separator) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			while(true) {
				String line = br.readLine();
				if(line == null) {
					break;
				}
				line = line.trim();
				if(line.length() == 0 || line.startsWith("#")) {
					continue;
				}
				int i = line.indexOf(separator);
				if(i == -1) {
					continue;
				}
				String key = line.substring(0, i);
				String value = line.substring(i + 1);
				
				try {
					configParser.parse(key, value);
				}catch (Exception e) {
					throw new RuntimeException("Parsing line: '" + line + "'");
				}
			}
		}catch (Exception e) {
			throw new RuntimeException("Reading InputStream: " + in, e);
		}finally {
			try {
				in.close();
			}catch (Exception e) {}
			try {
				br.close();
			}catch (Exception e) {}
		}
	}
	
	public static interface ConfigParser {
		
		public void parse(String key, String value);
		
	}

}
