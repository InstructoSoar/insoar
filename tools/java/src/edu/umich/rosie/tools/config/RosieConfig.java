package edu.umich.rosie.tools.config;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

public class RosieConfig {

	// A custom exception thrown if there is an error in the configuration file
	public static class RosieConfigException extends Exception {
		public RosieConfigException(String message){
			super(message);
		}
	}

	// A set of property names used in the config file
	public static final HashSet<String> PROP_NAMES = new HashSet<String>(
			Arrays.asList("agent-name", "agent-dir", "domain", "parser", 
					"sentence-source", "sentences-file", "world-file", "smem-config-file", 
					"custom-soar-file", "custom-smem-file"));
	
	// agent-name = <string> [OPTIONAL] - The name of the agent (used as root of file names)
	//   DEFAULT - The name of the config file (minus extension)
	public String agentName;
	
	// agent-dir = <string>  [OPTIONAL] - The directory the agent should be created in
	//    DEFAULT - The directory containing the config file
	public String agentDir;
	
	// domain = << magicbot tabletop internal fetch >>  [REQUIRED]
	//    The environment the agent is in (determines perception/action rules)
	public String domain;
	public static final HashSet<String> VALID_DOMAINS = new HashSet<String>(
			Arrays.asList("magicbot", "tabletop", "internal", "fetch"));
	
	// parser = << laird lucia >>  [OPTIONAL] - The parser the agent should use
	//    DEFAULT - laird
	public String parser;
	public static final String DEFAULT_PARSER = "laird";
	public static final HashSet<String> VALID_PARSERS = new HashSet<String>(
			Arrays.asList("laird", "lucia"));
	
	// sentence-source = << chat scripts >>  [OPTIONAL] - Where sentences come from
	//    DEFAULT - chat
	public String sentenceSource;
	public static final String DEFAULT_SENTENCE_SRC = "chat";
	public static final HashSet<String> VALID_SENTENCE_SRCS = new HashSet<String>(
			Arrays.asList("chat", "scripts"));
	
	// sentences-file = <filename> [OPTIONAL] - A file with a list of sentences
	//    DEFAULT - <agent-name>.sentences
	//    (File used by SentencesGenerator)
	public File sentencesFile;
	
	// world-file = <filename> [OPTIONAL] - A file with info about the top-state world 
	//    DEFAULT - <agent-name>.world
	//    (File used by WorldGenerator)
	public File worldFile;

	// smem-config-file = <filename> [OPTIONAL] - A file with info about what concepts to include from smem
	//    DEFAULT - <agent-name>.smem
	//    (File used by SmemConfigurator)
	public File smemConfigFile;
	
	// custom-soar-file = <filename> [OPTIONAL] - A file containing soar code that will be sourced by the agent
	//    DEFAULT - <agent-name>.soar
	public File customSoarFile;

	// custom-smem-file = <filename> [OPTIONAL] - A file containing smem adds that will be sourced by the agent
	//    DEFAULT - <agent-name>_smem.soar
	public File customSmemFile;

	// Any other properties in the file will be put into the created rosie.config file
	public HashMap<String, String> otherSettings;
	
	// rosieHome - the directory containing rosie
	public String rosieHome;
	
	public RosieConfig(File configFile, Properties props, String rosieHome) throws RosieConfigException {
		String configDir = configFile.getParent();
		System.out.println(configDir);
		
		// rosieHome
		this.rosieHome = rosieHome; 

		// agent-name
		if (props.containsKey("agent-name")){
			this.agentName = props.getProperty("agent-name");
		} else {
			this.agentName = configFile.getName();
			if (this.agentName.lastIndexOf(".") > 0){
				this.agentName = this.agentName.substring(0, this.agentName.lastIndexOf("."));
			}
		}
		
		// agent-dir
		if (props.containsKey("agent-dir")){
			this.agentDir = props.getProperty("agent-dir");
		} else {
			this.agentDir = configFile.getParent() + "/agent";
		}
		
		// domain
		if (props.containsKey("domain")){
			this.domain = props.getProperty("domain").toLowerCase();
			if(!VALID_DOMAINS.contains(this.domain)){
				throw new RosieConfigException("domain " + this.domain + " is not valid\n" + 
						"Must be one of: " + VALID_DOMAINS.toString());
			}
		} else {
			throw new RosieConfigException("Missing domain parameter");
		}
		
		// parser
		if (props.containsKey("parser")){
			this.parser = props.getProperty("parser").toLowerCase();
			if(!VALID_PARSERS.contains(this.parser)){
				throw new RosieConfigException("parser " + this.parser + " is not valid\n" + 
						"Must be one of: " + VALID_PARSERS.toString());
			}
		} else {
			parser = DEFAULT_PARSER;
		}
		
		// sentence-source
		if (props.containsKey("sentence-source")){
			this.sentenceSource = props.getProperty("sentence-source").toLowerCase();
			if(!VALID_SENTENCE_SRCS.contains(this.sentenceSource)){
				throw new RosieConfigException("sentence-source " + this.sentenceSource + " is not valid\n" + 
						"Must be one of: " + VALID_SENTENCE_SRCS.toString());
			}
		} else {
			this.sentenceSource = DEFAULT_SENTENCE_SRC;
		}
		
		// sentences-file
		if (props.containsKey("sentences-file")){
			this.sentencesFile = new File(configDir + "/" + props.getProperty("sentences-file"));
		} else {
			this.sentencesFile = new File(configDir + "/" + this.agentName + ".sentences");
		}
		
		// world-file
		if (props.containsKey("world-file")){
			this.worldFile = new File(configDir + "/" + props.getProperty("world-file"));
		} else {
			this.worldFile = new File(configDir + "/" + this.agentName + ".world");
		}
		
		// smem-config-file
		if (props.containsKey("smem-config-file")){
			this.smemConfigFile = new File(configDir + "/" + props.getProperty("smem-config-file"));
		} else {
			this.smemConfigFile = new File(configDir + "/" + this.agentName + ".smem");
		}
		
		// custom-soar-file
		if (props.containsKey("custom-soar-file")){
			this.customSoarFile = new File(configDir + "/" + props.getProperty("custom-soar-file"));
		} else {
			this.customSoarFile = new File(configDir + "/" + this.agentName + ".soar");
		}
		
		// custom-smem-file
		if (props.containsKey("custom-smem-file")){
			this.customSmemFile = new File(configDir + "/" + props.getProperty("custom-smem-file"));
		} else {
			this.customSmemFile = new File(configDir + "/" + this.agentName + "_smem.soar");
		}
		
		// otherSettings
		// Anything else in the config file will be 
		this.otherSettings = new HashMap<String, String>();
		for(Map.Entry<Object, Object> e : props.entrySet()){
			String name = (String)e.getKey();
			String val = (String)e.getValue();
			if(!PROP_NAMES.contains(name)){
				this.otherSettings.put(name, val);
			}
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("agent-name = " + this.agentName + "\n");
		sb.append("agent-dir = " + this.agentDir + "\n");
		sb.append("rosie-home = " + this.rosieHome + "\n");
		sb.append("domain = " + this.domain + "\n");
		sb.append("parser = " + this.parser + "\n");
		sb.append("sentence-source = " + this.sentenceSource + "\n");
		sb.append("sentences-file = " + (this.sentencesFile.exists() ? this.sentencesFile.getName() : "None") + "\n");
		sb.append("world-file = " + (this.worldFile.exists() ? this.worldFile.getName() : "None") + "\n");
		sb.append("smem-config-file = " + (this.smemConfigFile.exists() ? this.smemConfigFile.getName() : "None") + "\n");
		sb.append("custom-soar-file = " + (this.customSoarFile.exists() ? this.customSoarFile.getName() : "None") + "\n");
		sb.append("custom-smem-file = " + (this.customSmemFile.exists() ? this.customSmemFile.getName() : "None") + "\n");
		
		for(Map.Entry<String, String> e : this.otherSettings.entrySet()){
			sb.append(e.getKey() + " = " + e.getValue() + "\n");
		}
		
		return sb.toString();
	}
}