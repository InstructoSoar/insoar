package edu.umich.rosie.tools.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SmemConfigurator {
	public static HashMap<String, String> ltiMap = new HashMap<String, String>();
	
	public static ArrayList<String> configureSmem(File inputFile, File outputDir, File agentDir, String agentName){
		File smemDir = new File(outputDir, "/smem");
		if(!smemDir.exists()){
			smemDir.mkdir();
		}
		
		File templateDir = new File(agentDir, "/init-smem/templates");
		
		try {
			Writer smemSourceFile = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(smemDir, "/smem_source.soar"))));

			BufferedReader configReader = new BufferedReader(new FileReader(inputFile));
			
			String line;
			while((line = configReader.readLine()) != null){
				if(line.trim().startsWith("template")){
					String[] args = line.trim().split(" ");
					if(args.length > 1){
						File outputFile = new File(smemDir, "/" + args[1] + ".soar");
						writeTemplateFile(configReader, args[1], templateDir, outputFile);
						smemSourceFile.write("source " + outputFile.getName() + "\n\n");
					}
				}
			}
			
			smemSourceFile.close();
		} catch (Exception e){

			
		}

		
		
		try{
			Writer actionsFile = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(smemDir, "/actions.soar"))));
			HashMap<String, ArrayList<String> > actions = parseFile(new File(agentDir, "/init-smem/new-actions.soar"));
			for(String handle : actions.keySet()){
				actionsFile.write("smem --add {\n");
				for(String line : actions.get(handle)){
					actionsFile.write("  " + line + "\n");
				}
				actionsFile.write("}\n\n");
			}
			actionsFile.close();
		} catch (Exception e){
			
		}
		
		ArrayList<String> filesWritten = new ArrayList<String>();
//		try{
//			String actionsFilename = outputDir + "/smem_actions_" + agentName + ".soar";
//			Writer actionsFile = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(actionsFilename), "utf-8"));
//			actionsFile.write("# Placeholder for autogenerated actions smem file");
//			actionsFile.close();
//			filesWritten.add(actionsFilename);
//
//			String relationsFilename = outputDir + "/smem_relations_" + agentName + ".soar";
//			Writer relationsFile = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(relationsFilename), "utf-8"));
//			relationsFile.write("# Placeholder for autogenerated relations smem file");
//			relationsFile.close();
//			filesWritten.add(relationsFilename);
//
//			String propertiesFilename = outputDir + "/smem_properties_" + agentName + ".soar";
//			Writer propertiesFile = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(propertiesFilename), "utf-8"));
//			propertiesFile.write("# Placeholder for autogenerated properties smem file");
//			propertiesFile.close();
//			filesWritten.add(propertiesFilename);
//			
//		} catch (Exception e){
//			e.printStackTrace();
//		}	
		return filesWritten;
	}
	
	public static String mapLTIs(String line){
		// Replace all lti references with a numerical equivalent
		// (e.g. map @red1 --> @100110)
		String[] words = line.split("\\s+");
		for(String word : words){
			word = word.replace("(", "").replace(")", "");
			if(word.length() >= 2 && word.startsWith("@") && !Character.isDigit(word.charAt(1))){
				String lti;
				if (ltiMap.containsKey(word)){
					lti = ltiMap.get(word);
				} else {
					lti = "@" + new Integer(100000 + ltiMap.size()).toString();
					ltiMap.put(word, lti);
				}
				line = line.replace(word, lti);
			}
		}
		return line;
	}
	
	private static ArrayList<String> readTemplateFile(File templateDir, String templateName) throws IOException, FileNotFoundException {
		File templateFile = new File(templateDir, "/" + templateName + ".txt");
		if(!templateFile.exists()){
			throw new FileNotFoundException(templateFile.getAbsolutePath());
		}

		BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));
		ArrayList<String> template = new ArrayList<String>();
		
		String line;
		while((line = templateReader.readLine()) != null){
			template.add(line);
		}

		templateReader.close();
		return template;
	}
	
	
	public static ArrayList<String> fillTemplate(ArrayList<String> template, String handle, String[] words){
		StringBuilder wordTemplate = new StringBuilder();
		StringBuilder conceptTemplate = new StringBuilder();
		boolean hitConcept = false;
		for(String line : template){
			if(line.startsWith("(@_HANDLE_")){
				hitConcept = true;
			}
			line = line.replaceAll("_HANDLE_", handle);
			if(hitConcept){
				conceptTemplate.append(line + "\n");
			} else {
				wordTemplate.append(line + "\n");
			}
		}

		ArrayList<String> lines = new ArrayList<String>();
		for(String word : words){
			String newline = wordTemplate.toString().replaceAll("_WORD_", word);
			newline = mapLTIs(newline);
			lines.add(newline);
		}

		lines.add(mapLTIs(conceptTemplate.toString()));

		return lines;
	}
	
	private static void writeTemplateFile(BufferedReader configReader, String templateName, File templateDir, File outputFile){
		try {
			ArrayList<String> template = readTemplateFile(templateDir, templateName);
			
			Writer outputWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			outputWriter.write("### AUTOGENERATED smem file for the " + templateName + " template ###\n");
			outputWriter.write("smem --add {\n");
		
			String line;
			while((line = configReader.readLine()) != null){
				line = line.trim();
				if(line.startsWith("}")){
					break;
				}
				// handle is the first word
				String handle = line;
				if(line.indexOf(" ") != -1){
					handle = line.substring(0, line.indexOf(" "));
				}
				// any additional words in the line are spellings
				String[] words = new String[0];
				if(line.indexOf(" ") != -1){
					words = line.substring(line.indexOf(" ")+1).split(" ");
				}

				ArrayList<String> filledTemplate = fillTemplate(template, handle, words);
			
				for(String l : filledTemplate){
					outputWriter.write("  " + l + "\n");
				}
				outputWriter.write("\n");
			}
			
			outputWriter.write("}\n");
			outputWriter.close();
		} catch (Exception e){
			System.err.println("ERROR reading template file " + templateName);
			System.err.println(e.getMessage());
		}
	}
	
	private static HashMap<String, ArrayList<String> > parseFile(File file){

		HashMap<String, ArrayList<String> > concepts = new HashMap<String, ArrayList<String> >();
		System.out.println(file.getAbsolutePath());

		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			ArrayList<String> currentLines = new ArrayList<String>();
			String handle = null;
			while ((line = reader.readLine()) != null){
				// Ignore lines starting with #!
				if (line.contains("#!")){
					continue;
				}
				
				// New concept definition
				if (line.contains("+")){
					// Wrap up old definition
					if(handle != null){
						if(concepts.containsKey(handle)){
							System.err.println("ERROR: Concept map already contains the key " + handle);
						} else {
							boolean content = false;
							for(String l : currentLines){
								if(!l.trim().startsWith("#")){
									content = true;
									break;
								}
							}
							if(content){
								concepts.put(handle, currentLines);
							}
						}
					}
					
					// Start a new concept definition block
					int plusIndex = line.indexOf("+") + 1;
					int endIndex = line.indexOf(" ", plusIndex);
					if(endIndex == -1){
						endIndex = line.length();
					}
					handle = line.substring(plusIndex, endIndex);
					currentLines = new ArrayList<String>();
					currentLines.add(line);
				} else if(line.trim().length() > 0){
					// Add the line to the current block
					currentLines.add(mapLTIs(line));
				}
			}
			
			reader.close();
		} catch (Exception e){
			System.err.println("PARSING ERROR in file " + file.getAbsolutePath());
			System.err.println(e.toString());
		}

		return concepts;
	}



    public static void main(String[] args) {
    	if (args.length < 2){
    		System.err.println("SmemConfigurator requires 2 argument:\n" + 
    							"  1: The input filename with the smem configuration information\n" + 
    							"  2: The name to use when creating files (e.g. smem_actions_<name>.soar\n" + 
    							"  3 [OPT]: The root rosie agent directory (Defaults to $ROSIE_HOME)\n");
    		System.exit(1);
    	}
    	
    	File inputFile = new File(args[0]);
    	String agentName = args[1];
    	
    	File rosieDir = null;
    	if (args.length >= 3){
    		rosieDir = new File(args[2]);
    	} else {
    		String rosieHome = System.getenv("ROSIE_HOME");
    		if (rosieHome == null){
    			System.err.println("$ROSIE_HOME environment variable is not set");
    			System.exit(1);
    		}
    		rosieDir = new File(rosieHome);
    	}
    	
    	File outputDir = new File(System.getProperty("user.dir"));
    	
    	SmemConfigurator.configureSmem(inputFile, outputDir, rosieDir, agentName);
    }

}
