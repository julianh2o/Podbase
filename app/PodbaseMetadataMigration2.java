// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import models.DatabaseImage;
import models.Project;
import models.Template;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.yaml.snakeyaml.Yaml;

import services.ImageService;
import services.PathService;

public class PodbaseMetadataMigration2 {
	public final static String RECORD_SEPARATOR = "\0";
	public final static String FIELD_SEPARATOR = "\\|\\|";
	
	public final static boolean testMode = false;
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
	static String NOW = sdf.format(new Date());
	
	public static int imagePathIndex = 0;
	public static HashMap<Path,Integer> imagePathMap = new HashMap<Path,Integer>();
	public static List<String> missingImages = new LinkedList<String>();
	
	public static void main(String[] args) throws Exception {
		System.out.println("Running data migration");
		
		System.out.println("Reading projects..");
		List<ProjectEntry> projects = dataFromFile("./migrate/projects.data",ProjectEntry.class);
//		HashMap<Integer,String> projectMap = parseProjectMap(projects);
		System.out.println("Found "+projects.size()+ " projects.");
		
		System.out.println("Reading tags..");
		List<TagEntry> tags = dataFromFile("./migrate/tags.data",TagEntry.class);
		System.out.println("Found "+tags.size()+ " tags.");
		
		System.out.println("Reading templates..");
		List<TemplateEntry> templates = dataFromFile("./migrate/templates.data",TemplateEntry.class);
		System.out.println("Found "+templates.size()+ " templates.");
		
		System.out.println("Reading template fields..");
		List<TemplateFieldEntry> templateFields = dataFromFile("./migrate/template_fields.data",TemplateFieldEntry.class);
		System.out.println("Found "+templateFields.size()+ " templateFields.");
		
		int entryCount = tags.size() + templates.size() + templateFields.size();
		
		System.out.println("Generating Project SQL");
		String projectSql = generateSql((List<AbstractEntry>)(List<?>)projects);
		System.out.println("Generating Attribute SQL");
		String imageAttributes = generateSql((List<AbstractEntry>)(List<?>)tags);
		System.out.println("Generating Image SQL");
		String databaseImages = generateDatabaseImageSql();
		
		System.out.println("Generating Template SQL");
		String templateSql = generateSql((List<AbstractEntry>)(List<?>)templates);
		System.out.println("Generating Field SQL");
		String fieldsSql = generateSql((List<AbstractEntry>)(List<?>)templateFields);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./database.sql")));
		bw.append(projectSql);
		bw.append("\n\n");
		bw.append(databaseImages);
		bw.append("\n\n");
		bw.append(imageAttributes);
		bw.append("\n\n");
		bw.append(templateSql);
		bw.append("\n\n");
		bw.append(fieldsSql);
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(new File("./missingImages.txt")));
		for (String img : missingImages) {
			bw.append(img+"\n");
		}
		bw.close();
		
		System.out.println("Migration completed successfully!");
	}
	
	private static String generateDatabaseImageSql() throws IOException {
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for (Entry<Path,Integer> entry : imagePathMap.entrySet()) {
			if (!first) sb.append(",");
			
			Path path = entry.getKey();
			int imageId = entry.getValue();
			
			//id,now,now,imported,path
			sb.append(String.format("(%d,'%s','%s',0x0,'%s','%s')",imageId,NOW,NOW,PathService.getRelativeString(path),PathService.calculateImageHash(path)));
			first = false;
		}
		
		return "INSERT INTO `DatabaseImage` VALUES "+sb.toString()+";";
	}
	
	private static String generateSql(List<AbstractEntry> entries) {
		StringBuilder records = new StringBuilder();
		
		String tableName = entries.get(0).getTableName();
		String before = "INSERT INTO `"+tableName+"` VALUES ";
		String after = ";";
		
		boolean needsPrelude = true;
		long lastMilestone = System.currentTimeMillis();
		int i = 0;
		for (AbstractEntry entry : entries) {
			i++;
			long current = System.currentTimeMillis();
			if (current - lastMilestone > 1000*30) {
				System.out.println(i +" / "+entries.size());
				lastMilestone = current;
			}
			
			String insert = entry.getInsertValues();
			if (insert != null) {
				if (needsPrelude) {
					needsPrelude = false;
					records.append(before);
				} else {
					records.append(",");
				}
				records.append(insert);
			}
			
			if (i % 100 == 0) {
				records.append(after+"\n");
				needsPrelude = true;
			}
		}
		if (!needsPrelude) records.append(after);
		
		return records.toString();
	}

	private static String formatMs(double ms) {
		DecimalFormat df = new DecimalFormat("###0.00");
		if (ms < 3000) return ms+"ms";
		if (ms < 1000*60*3) return df.format((double)ms/1000)+"s";
		int minutes = (int)(ms/(1000*60));
		int seconds = (int)((ms - minutes*1000*60) / 1000);
		return String.format("%d minutes, %d seconds",minutes,seconds);
	}
	
//	private static void importTags(HashMap<Integer,String> projectMap, List<TagEntry> tags) {
//		for (TagEntry tag : tags) {
//			
//			try {
//				System.out.println("fixed: "+PathService.fixCaseResolve(tag.path));
//				
//				System.out.println("attr: "+key+" : "+value);
//			} catch (FileNotFoundException fnf) {
//				System.out.println("!! "+fnf.getMessage());
//				continue;
//			}
//		}
//	}
	
//	private static void importTemplates(HashMap<Integer,String> projectMap, List<TemplateEntry> templates, List<TemplateFieldEntry> templateFields) {
//		HashMap<Integer,Template> map = new HashMap<Integer,Template>();
//		for (TemplateEntry template : templates) {
//			System.out.println("create template "+template.projectId+" "+template.name);
//		}
//		
//		for (TemplateFieldEntry field : templateFields) {
//			System.out.println("adding field: "+field.name+" "+field.description+" "+!field.show);
//		}
//	}
	
	public static void print(Object...objects) {
		String out = "";
		for (Object o : objects) {
			if (out.length() > 0) out += " ";
			out += o == null ? null : o.toString();
		}
		System.out.println(out);
	}

//	public static HashMap<Integer,String> parseProjectMap(List<ProjectEntry> entries) {
//		HashMap<Integer,String> projectMap = new HashMap<Integer,String>();
//		for(ProjectEntry entry : entries) {
//			projectMap.put(entry.projectId, entry.name);
//		}
//		return projectMap;
//	}
	
	public void printEntries(List<? extends AbstractEntry> entries) {
		System.out.println("Printing "+entries.size()+" entries!");
		for(AbstractEntry entry : entries) {
			System.out.println(entry.toString());
		}
	}
	
	public static <T extends AbstractEntry> List<T> dataFromFile(String path, Class<T> klass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		File f = new File(path);
		if (!f.exists()) throw new FileNotFoundException(path);
		
		List<T> entries = parseFile(f, klass);
		return entries;
	}
	
	public static <T extends AbstractEntry> List<T> parseFile(File f, Class<T> klass) throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String fileContents = FileUtils.readFileToString(f);
		fileContents = fixFileContents(fileContents);
		
		List<T> entries = new LinkedList<T>();
		int i = 0;
		int ignore = 0;
		for (String line : fileContents.split(RECORD_SEPARATOR,-1)) {
			i++;
			if (testMode && i > 10) break;
			if (line.trim().length() == 0) continue;
			
			String readable = line.replace(FIELD_SEPARATOR, "[###]");
			if (line.contains("Fatal error")) {
				ignore ++;
				//System.out.println(klass.toString()+": Ignoring line "+i+": "+readable);
			}
			try {
				T entry = klass.getConstructor(String.class).newInstance(line);
				entries.add(entry);
			} catch (Exception e) {
				System.out.println(klass.toString()+": Illegal entry on line "+i+": "+readable+" ( "+e.getMessage()+")");
				//e.printStackTrace();
				return entries;
			}
		}
		if (ignore > 0) System.out.println("Ignored "+ignore+" entries");
		
		return entries;
	}
	
	public static String fixFileContents(String contents) throws UnsupportedEncodingException {
		return new String(contents.getBytes("US-ASCII"));
	}
	
	private static abstract class AbstractEntry {
		protected abstract String getTableName();
		protected abstract String getInsertValues();
		
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
			String[] lineFields = line.split(FIELD_SEPARATOR,-1);
			
			if (fields.length != lineFields.length) throw new IllegalArgumentException("Length mismatch: "+fields.length + " vs " + lineFields.length+"  ("+StringUtils.join(lineFields,"--")+")"); 
			
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
	
	public static class TagEntry extends AbstractEntry {
		public int projectId;
		public String tagtype; //eg "user"
		public String path;
		public String tagContents;
		
		public TagEntry(String line) {
			super(line);
		}
		
		@Override
		protected String getTableName() {
			return "ImageAttribute";
		}

		@Override
		protected String getInsertValues() {
			String[] keyValue = getKeyValue();
			String key = keyValue[0];
			String value = keyValue[1];
			//(1,'2013-06-11 20:39:09','2013-06-11 20:39:09','tags','^A','\0','unmyelinated axon',1,NULL,10)
			//												      isData,hidden   ,value,     imageId, linkedAttr,projectId
			
			try {
				if (path.equals("undefined")) return null;
				Path convertedPath = PathService.fixCaseResolve(path);
				int imageId;
				
				if (imagePathMap.containsKey(convertedPath)) {
					imageId = imagePathMap.get(convertedPath);
				} else {
					imageId = ++imagePathIndex;
					imagePathMap.put(convertedPath, imageId);
				}
				
				return String.format("(%d,'%s','%s','%s',0x1,0x0,'%s',%d,NULL,%d)",generateId(),NOW,NOW,escape(key),escape(value),imageId,projectId);
			} catch (FileNotFoundException fnf) {
				missingImages.add(path);
				return null;
			}
		}
		
		private String[] getKeyValue() {
			String key = "tags";
			String value = tagContents;
			
			if (value.contains(":")) {
				String[] split = value.split(":",2);
				key = split[0];
				value = split[1];
			}
			
			return new String[] {key,value};
		}
		
		private static int id = 1;
		private static int generateId() {
			return id++;
		}
	}
	
	public static class ProjectEntry extends AbstractEntry {
		public int projectId;
		public String name;
		
		public ProjectEntry(String line) {
			super(line);
		}

		@Override
		protected String getTableName() {
			return "PermissionedModel";
		}

		@Override
		protected String getInsertValues() {
			return String.format("('Project',%d,'%s','%s','%s',0x0,NULL)",projectId,NOW,NOW,escape(name));
		}
	}
	
	public static class TemplateEntry extends AbstractEntry {
		public int templateId;
		public int projectId;
		public String name;
		
		public TemplateEntry(String line) {
			super(line);
		}

		@Override
		protected String getTableName() {
			return "Template";
		}

		@Override
		protected String getInsertValues() {
			//id,now,now,name,projectid
			return String.format("(%d,'%s','%s','%s',%d)",templateId,NOW,NOW,escape(name),projectId);
		}
	}
	
	public static class TemplateFieldEntry extends AbstractEntry {
		public int templateId;
		public int ordering;
		public boolean show;
		public String name;
		public String description;
		
		public TemplateFieldEntry(String line) {
			super(line);
		}

		@Override
		protected String getTableName() {
			return "TemplateAttribute";
		}

		@Override
		protected String getInsertValues() {
			//id,now,now,desc,hidden,name,sort,type,templateid
			return String.format("(%d,'%s','%s','%s',%s,'%s',%d,'%s',%d)",generateId(),NOW,NOW,escape(description),(show?"0x0":"0x1"),escape(name),ordering,"normal",templateId);
		}
		
		private static int id = 1;
		private static int generateId() {
			return id++;
		}
	}
	
	public static String escape(String s) {
		return s.replace("'", "\\'");
	}
}
