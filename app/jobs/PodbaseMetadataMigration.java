package jobs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import models.DatabaseImage;
import models.Project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.yaml.snakeyaml.Yaml;

import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import services.PathService;

@OnApplicationStart
public class PodbaseMetadataMigration extends Job {
	public void doJob() throws Exception {
		File f = new File("./migrate/data.yaml");
		
		List<Entry> entries = parseFile(f);
		Project project = null; //Project.findById(new Long(1));
		
		int i=0;
		for(Entry entry : entries) {
			if (Play.id.equals("dev")) System.out.println("Importing metadata: "+entry.path);
			
			System.out.println("entry path: "+entry.path);
			DatabaseImage image = DatabaseImage.forPath(PathService.resolve("/"+entry.path));
			for(String key : entry.data.keySet()) {
				image.addAttribute(project, key,entry.data.get(key), true);
			}
			
			if ("dev".equals(Play.configuration.get("application.mode")) && i++ > 5) return; //cut off after 5 in dev mode
		}
	}
	
	public List<Entry> parseFile(File f) throws IOException {
		String fileContents = FileUtils.readFileToString(f);
		fileContents = fixFileContents(fileContents);
		
		Yaml yaml = new Yaml();
        Object o = yaml.load(fileContents);
        
        List<Entry> convertedEntries = new LinkedList<Entry>();
        
        ArrayList entries = (ArrayList)o;
        for (Object entry : entries) {
        	Entry e = parseEntry((LinkedHashMap<String,Object>)entry);
        	convertedEntries.add(e);
        }
        
		return convertedEntries;
	}
	
	public Entry parseEntry(LinkedHashMap<String,Object> map) {
		LinkedHashMap<String,Object> entry = (LinkedHashMap<String,Object>)map.get("entry");
		String path = (String)entry.get("path");
		
		Entry en = new Entry(path);
		
		ArrayList<ArrayList<String>> tags = (ArrayList<ArrayList<String>>)entry.get("tags");
		
		for(ArrayList<String> tag : tags) {
			String key = tag.get(0);
			String value = tag.get(1);
			
			en.data.put(key, value);
		}
		
		return en;
	}
	
	public String fixFileContents(String contents) throws UnsupportedEncodingException {
		return new String(contents.getBytes("US-ASCII"));
	}
	
	private class Entry {
		String path;
		HashMap<String,String> data;
		
		public Entry(String path) {
			this.path = path;
			this.data = new HashMap();
		}
	}
}
