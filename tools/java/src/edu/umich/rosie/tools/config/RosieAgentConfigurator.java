package edu.umich.rosie.tools.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class RosieAgentConfigurator {
	public static void ConfigureAgent(RosieConfig config){
		try{
			// Create the Agent Directory
			File agentDir = new File(config.agentDir);
			if(!agentDir.exists()){
				if(!agentDir.mkdir()){
					System.err.println("ERROR: ConfigureAgent could not create directory " + config.agentDir);
					return;
				}
			}

			// Different filenames used by the agent
			String agentConfigFilename = config.agentDir + "/rosie." + config.agentName + ".config";
			String agentSourceFilename = config.agentDir + "/source_" + config.agentName + ".soar";
			String smemSourceFilename = config.agentDir + "/smem_source_" + config.agentName + ".soar";
			
			// Write the 3 files
			writeAgentConfigFile(config, agentConfigFilename, agentSourceFilename, smemSourceFilename);
			writeAgentSourceFile(config, agentSourceFilename);
			writeSmemSourceFile(config, smemSourceFilename);

		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void writeAgentConfigFile(RosieConfig config, String agentConfigFilename, 
			String agentSourceFilename, String smemSourceFilename) throws IOException {
		// Create the agent config file (Sourced by the java Rosie agent)
		Writer agentConfigFile = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(agentConfigFilename), "utf-8"));
		
		// Config File Beginning
		agentConfigFile.write("# This Rosie config file was autogenerated by the RosieAgentConfigurator\n");
		agentConfigFile.write("agent-name = " + config.agentName + "\n\n");
		
		// Agent Source Information
		agentConfigFile.write("agent-source = " + agentSourceFilename + "\n");
		agentConfigFile.write("smem-source = " + smemSourceFilename + "\n\n");

		// messages-file
		if (config.sentenceSource.equals("chat") && config.sentencesFile.exists()){
			agentConfigFile.write("messages-file = " + config.sentencesFile.getAbsolutePath() + "\n\n");
		}
		
		// Other Settings
		for(Map.Entry<String, String> e : config.otherSettings.entrySet()){
			agentConfigFile.write(e.getKey() + " = " + e.getValue() + "\n");
		}
		agentConfigFile.close();
	}
	
	private static void writeAgentSourceFile(RosieConfig config, String agentSourceFilename) throws IOException {
		Writer agentSourceFile = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(agentSourceFilename), "utf-8"));
		agentSourceFile.write("# This file was autogenerated by the RosieAgentConfigurator\n");
		agentSourceFile.write("# This is the root agent soar file to source for the '" + config.agentName + "' agent.\n\n");
		agentSourceFile.write("pushd " + config.agentDir + "\n\n");
		
		// Source the proper agent soar file
		agentSourceFile.write("# Sourcing the proper agent file for the specified domain\n");
		agentSourceFile.write("source " + config.rosieHome + "/agent/" + config.domain + "_source.soar\n\n");

		// Sentences
		if (config.sentenceSource.equals("scripts") && config.sentencesFile.exists()){
			File sentenceSourceFile = new File(config.agentDir + "/sentences_" + config.agentName + ".soar");
			SentencesGenerator.generateRosieSentences(config.sentencesFile, sentenceSourceFile);
			agentSourceFile.write("# Sourcing a list of scripted sentences to autorun\n");
			agentSourceFile.write("source " + sentenceSourceFile.getAbsolutePath() + "\n\n");
		}
		
		// World
		if (config.worldFile.exists()){
			File rosieWorldFile = new File(config.agentDir + "/world_" + config.agentName + ".soar");
			WorldGenerator.generateRosieWorld(config.worldFile, rosieWorldFile);
			agentSourceFile.write("# Sourcing a specification for the top-state world\n");
			agentSourceFile.write("source " + rosieWorldFile.getAbsolutePath() + "\n\n");
		}
		
		// Custom soar file
		if (config.customSoarFile.exists()){
			agentSourceFile.write("# Sourcing custom soar code specific to this agent\n");
			agentSourceFile.write("source " + config.customSoarFile.getAbsolutePath() + "\n\n");
		}
		
		// Finish writing the agent source file
		agentSourceFile.write("popd\n");
		agentSourceFile.close();	
	}
	
	private static void writeSmemSourceFile(RosieConfig config, String smemSourceFilename) throws IOException {
		Writer smemSourceWriter = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(smemSourceFilename), "utf-8"));
		smemSourceWriter.write("# This file was autogenerated by the RosieAgentConfigurator\n");
		smemSourceWriter.write("# This file will source different files that will initialize the agent's semantic memory\n");
		smemSourceWriter.write("pushd " + config.agentDir + "\n\n");

		File smemDir = new File(config.rosieHome + "/agent/init-smem");
		
		// Smem
		if (config.smemConfigFile.exists()){
			File outputDir = new File(config.agentDir);
			File rosieDir = new File(config.rosieHome + "/agent");
			File smemSourceFile = SmemConfigurator.configureSmem(config.smemConfigFile, outputDir, rosieDir, config.agentName);
			smemSourceWriter.write("# This file will source the smem files that were created by the SmemConfiguator tool\n");
			smemSourceWriter.write("source " + smemSourceFile.getAbsolutePath() + "\n");
			smemSourceWriter.write("\n");
		}
		
		// Custom smem file
		if (config.customSmemFile.exists()){
			smemSourceWriter.write("# Sourcing custom smem information specific to this agent\n");
			smemSourceWriter.write("source " + config.customSmemFile.getAbsolutePath() + "\n\n");
		}

		// Finish writing the smem source file
		smemSourceWriter.write("popd\n");
		smemSourceWriter.close();
	}
	
    public static void main(String[] args) {
    	if (args.length == 0){
    		System.err.println("RosieAgentConfigurator expects 1 argument\n" + 
    							"  1: the filename of the rosie configuration file\n" + 
    							"  2 [OPT]: the rosie directory (defaults from $ROSIE_HOME environment variable");
    		System.exit(0);
    	}
    	
    	String configFilename = args[0];
    	
    	File configFile = new File(System.getProperty("user.dir") + "/" + configFilename);
    	if(!configFile.exists()){
    		System.err.println("\nThe file " + configFilename + " does not exist");
    		System.exit(0);
    	}

    	
        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader(configFile));
		} catch (IOException e) {
			System.out.println("IO Error reading config file " + configFilename);
			e.printStackTrace();
			return;
		}
        
    	String rosieHome = null;
    	if (args.length >= 2){
    		rosieHome = args[2];
    	} else {
    		rosieHome = System.getenv("ROSIE_HOME");
    		if (rosieHome == null){
    			System.err.println("$ROSIE_HOME environment variable is not set");
    			System.exit(1);
    		}
    	}
        
        try{
        	RosieConfig config = new RosieConfig(configFile, props, rosieHome);
        	System.out.println(config.toString());
        	ConfigureAgent(config);
        } catch (RosieConfig.RosieConfigException e){
        	System.err.println("Rosie Configuration Error: " + e.getMessage());
        }
    }
}
