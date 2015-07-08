package com.keyes.youtube;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private Properties configuration;
    private String configurationFile = "config.ini";

    public Config(String configurationFile) {
    	this.configurationFile=configurationFile;
        configuration = new Properties();
    }

    public Properties getConfig() {
    	return configuration;
    }
    
    public boolean load() {
        boolean retval = false;

        try {
            //configuration.load(new FileInputStream(this.configurationFile));
            configuration.load(Playomatic.class.getResourceAsStream(this.configurationFile));
            retval = true;
        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }

        return retval;
    }

    public boolean store() {
        boolean retval = false;

        try {
            configuration.store(new FileOutputStream(this.configurationFile), null);
            retval = true;
        } catch (IOException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }

        return retval;
    }

    public void set(String key, String value) {
        configuration.setProperty(key, value);
    }

    public String get(String key) {
        return configuration.getProperty(key);
    }
}
