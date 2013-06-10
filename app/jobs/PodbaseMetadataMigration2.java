package jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import models.DatabaseImage;
import models.Project;
import models.Template;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.yaml.snakeyaml.Yaml;

import play.Play;
import play.cache.Cache;
import play.exceptions.JavaExecutionException;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.search.Search;
import play.modules.search.store.FilesystemStore;
import services.PathService;

public class PodbaseMetadataMigration2 extends MonitoredJob {
	ProgressCounter pc;
	
	public void doJob() throws Exception {
		((FilesystemStore)Search.getCurrentStore()).sync = false;
		
		List<ProjectEntry> projects = dataFromFile("./migrate/projects.data",ProjectEntry.class);
		HashMap<Integer,String> projectMap = parseProjectMap(projects);
		
		List<TagEntry> tags = dataFromFile("./migrate/tags.data",TagEntry.class);
		List<TemplateEntry> templates = dataFromFile("./migrate/templates.data",TemplateEntry.class);
		
		List<TemplateFieldEntry> templateFields = dataFromFile("./migrate/template_fields.data",TemplateFieldEntry.class);
		
		int entryCount = tags.size() + templates.size() + templateFields.size();
		pc = new ProgressCounter(entryCount);
		
		importTemplates(projectMap, templates,templateFields);
		
		for (TagEntry tag : tags) {
			pc.inc();
			String projectName = projectMap.get(tag.projectId);
			Project project = Project.get(projectName);
			if (project == null) {
				System.out.println("No project found! : "+projectName);
				continue;
			}
			
			String key = "tags";
			String value = tag.tagContents;
			
			if (value.contains(":")) {
				String[] split = value.split(":",2);
				key = split[0];
				value = split[1];
			}
			
			try {
				DatabaseImage image = DatabaseImage.forPath(PathService.fixCaseResolve(tag.path));
				image.addAttribute(project, key, value, true);
			} catch (FileNotFoundException fnf) {
				System.out.println("!! "+fnf.getMessage());
			}
		}
		
//		printEntries(tags);
//		printEntries(templates);
//		printEntries(templateFields);
		((FilesystemStore)Search.getCurrentStore()).sync = true;
	}
	
	class ProgressCounter {
		int current;
		int total;
		
		public ProgressCounter(int total) {
			this.total = total;
		}
		
		public void inc() {
			current++;
			setProgress(current, total);
		}
	}
	
	private void importTemplates(HashMap<Integer,String> projectMap, List<TemplateEntry> templates, List<TemplateFieldEntry> templateFields) {
		HashMap<Integer,Template> map = new HashMap<Integer,Template>();
		for (TemplateEntry template : templates) {
			pc.inc();
			String projectName = projectMap.get(template.projectId);
			Project project = Project.get(projectName);
			if (project == null) {
				System.out.println("No project found! : "+project);
				continue;
			}
			
			Template newTemplate = null;
			newTemplate = Template.find("byName", template.name).first();
			
			if (newTemplate != null) {
				newTemplate.delete();
			}
			
			newTemplate = new Template(project,template.name);
			newTemplate.save();
			
			map.put(template.templateId, newTemplate);
		}
		
		for (TemplateFieldEntry field : templateFields) {
			pc.inc();
			Template template = map.get(field.templateId);
			if (template == null) {
				System.out.println("Template is null! "+field.templateId);
				continue;
			}
			template.addAttribute(field.name, field.description, !field.show);
		}
	}
	
	public static void print(Object...objects) {
		String out = "";
		for (Object o : objects) {
			if (out.length() > 0) out += " ";
			out += o == null ? null : o.toString();
		}
		System.out.println(out);
	}

	public HashMap<Integer,String> parseProjectMap(List<ProjectEntry> entries) {
		HashMap<Integer,String> projectMap = new HashMap<Integer,String>();
		for(ProjectEntry entry : entries) {
			projectMap.put(entry.projectId, entry.name);
		}
		return projectMap;
	}
	
	public void printEntries(List<? extends AbstractEntry> entries) {
		System.out.println("Printing "+entries.size()+" entries!");
		for(AbstractEntry entry : entries) {
			System.out.println(entry.toString());
		}
	}
	
	public <T extends AbstractEntry> List<T> dataFromFile(String path, Class<T> klass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		File f = new File(path);
		if (!f.exists()) throw new FileNotFoundException(path);
		
		List<T> entries = parseFile(f, klass);
		return entries;
	}
	
	public <T extends AbstractEntry> List<T> parseFile(File f, Class<T> klass) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String fileContents = FileUtils.readFileToString(f);
		fileContents = fixFileContents(fileContents);
		
		List<T> entries = new LinkedList<T>();
		int i = 0;
		for (String line : fileContents.split("\0\0")) {
			i++;
			if (line.trim().length() == 0) continue;
			if (line.contains("Fatal error")) {
				System.out.println("Ignoring line "+i+": "+line.trim());
			}
			try {
				T entry = klass.getConstructor(PodbaseMetadataMigration2.class, String.class).newInstance(PodbaseMetadataMigration2.this,line);
				entries.add(entry);
			} catch (Exception e) {
				System.out.println("Illegal entry on line "+i+": "+line);
			}
		}
		
		return entries;
	}
	
	public String fixFileContents(String contents) throws UnsupportedEncodingException {
		return new String(contents.getBytes("US-ASCII"));
	}
	
	private abstract class AbstractEntry {
		protected String[] getFields() {
			Field[] fields = this.getClass().getFields();
			String[] stringFields = new String[fields.length];
			
			for(int i=0; i<fields.length; i++) {
				stringFields[i] = fields[i].getName();
			}
			
			return stringFields;
		}
		
		public AbstractEntry(String line) {
			setFields(line);
		}
		
		public void setFields(String line) {
			String[] fields = getFields();
			String[] lineFields = line.split("\0");
			
			if (fields.length != lineFields.length) throw new IllegalArgumentException("Length mismatch: "+fields.length + " vs " + lineFields.length); 
			
			for(int i=0; i< fields.length; i++) {
				setAttribute(fields[i],lineFields[i]);
			}
		}
		
		private void setAttribute(String attribute, String value)  {
			try {
				Field field = this.getClass().getField(attribute);
				if (field.getType() == Integer.TYPE) {
					field.setInt(this, Integer.parseInt(value));
				} else if (field.getType() == Boolean.class){
					field.setBoolean(this, Boolean.parseBoolean(value));
				} else if (field.getType() == String.class){
					field.set(this, value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public String toString() {
			String out = "";
			String[] fields = getFields();
			for (String fieldName : fields) {
				try {
					if (out != "") out += ", ";
					Field field = this.getClass().getField(fieldName);
					String value = field.get(this).toString();
					
					out += fieldName +" = " + value;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return "["+this.getClass().getSimpleName()+": "+out+"]";
		}
	}
	
	public class TagEntry extends AbstractEntry {
		public int projectId;
		public String tagtype;
		public String path;
		public String tagContents;
		
		public TagEntry(String line) {
			super(line);
		}
	}
	
	public class ProjectEntry extends AbstractEntry {
		public int projectId;
		public String name;
		
		public ProjectEntry(String line) {
			super(line);
		}
	}
	
	public class TemplateEntry extends AbstractEntry {
		public int templateId;
		public int projectId;
		public String name;
		
		public TemplateEntry(String line) {
			super(line);
		}
	}
	
	public class TemplateFieldEntry extends AbstractEntry {
		public int templateId;
		public int ordering;
		public boolean show;
		public String name;
		public String description;
		
		public TemplateFieldEntry(String line) {
			super(line);
		}
	}
}
