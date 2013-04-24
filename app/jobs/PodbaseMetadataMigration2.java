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
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.search.Search;
import play.modules.search.store.FilesystemStore;
import services.PathService;

public class PodbaseMetadataMigration2 extends MonitoredJob {
	public void doJob() throws Exception {
		((FilesystemStore)Search.getCurrentStore()).sync = false;
		
		File f = new File("./migrate/tagdump.csv");
		if (!f.exists()) return;
		
		List<Entry> entries = parseFile(f);
		Project project = null;
		
		int i=0;
		System.out.println("Read "+entries.size()+" entries!");
		for(Entry entry : entries) {
			//if (Play.id.equals("dev")) System.out.println("Importing metadata: "+entry.path);
			
			//System.out.println("Entry: "+entry.toString());
			
			//DatabaseImage image = DatabaseImage.forPath(PathService.resolve("/"+entry.path));
			//for(String key : entry.data.keySet()) {
				//image.addAttribute(project, key,entry.data.get(key), true);
			//}
			
			setProgress(i, entries.size());
			
			i++;
			//if ("dev".equals(Play.configuration.get("application.mode")) && i > 5) return; //cut off after 5 in dev mode
		}
		
		((FilesystemStore)Search.getCurrentStore()).sync = true;
	}
	
	public List<Entry> parseFile(File f) throws IOException {
		String fileContents = FileUtils.readFileToString(f);
		fileContents = fixFileContents(fileContents);
		
		List<Entry> entries = new LinkedList<Entry>();
		int i = 0;
		for (String line : fileContents.split("\n")) {
			i++;
			if (line.trim().length() == 0) continue;
			if (line.contains("Fatal error")) {
				System.out.println("Found 'Fatal Error' in line: "+i);
			}
			try {
				Entry entry = new Entry(line);
				entries.add(entry);
			} catch (IllegalArgumentException e) {
				System.out.println("Illegal entry on line: "+i);
			}
		}
		
		return entries;
	}
	
	public String fixFileContents(String contents) throws UnsupportedEncodingException {
		return new String(contents.getBytes("US-ASCII"));
	}
	
	private class Entry {
		int projectId;
		String tagtype;
		String path;
		
		String key;
		String value;
		
		public Entry(String line) {
			String[] fields = line.split("\\|");
			if (fields.length != 4) {
				throw new IllegalArgumentException("Expected 4 fields! Found "+fields.length);
			}
			
			this.projectId = Integer.parseInt(fields[0]);
			this.tagtype = fields[1];
			this.path = fields[2];
			String rawValue = fields[3];
			
			String[] valueSplit = rawValue.split(":");
			if (valueSplit.length == 2) {
				this.key = valueSplit[0];
				this.value = valueSplit[1];
			} else {
				this.key = "tag";
				this.value = rawValue;
			}
		}
		
		public String toString() {
			return projectId+", "+tagtype+", "+path+", "+key+", "+value;
		}
	}
}
