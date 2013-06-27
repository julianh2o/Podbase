package access;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

public enum AccessType {
	ROOT("Grants ultimate access","user"),
	CREATE_PROJECT("Allows user to create new projects","user"),
	DELETE_PROJECT("Allows user to delete projects","user"),
	CREATE_PAPER("Allows user to create papers","user"),
	CREATE_USERS("Allows user to create users","user"),
	
	LISTED("The user has been associated with the entity in question","paper","project"),
	VISIBLE("The user can see this entity in lists","paper","project"),
	OWNER("The user is considered an owner of this entity","paper","project"),
	ADMINISTRATOR("A user with elevated access rights","paper","project"),
	MANAGE_PERMISSIONS("The user is allowed to manage permissions for this entity","paper","project","user"),
	EDITOR("The user is an editor of this entity","paper","project"),
	DATA_EDITOR("The user is an data editor of this entity","paper","project"),
	NO_WATERMARK("The user can view the images without a watermark","project"),
	
	PROJECT_SET_TEMPLATE("The user can change the template associated with directories","project"),
	PROJECT_MANAGE_TEMPLATES("The user can edit templates associated with this project","project"),
	PROJECT_FILE_UPLOAD("The user can upload files to this project","project"),
	PROJECT_FILE_DELETE("The user can delete files from this project","project");
	//PROJECT_MANAGE_DIRECTORIES("The user can manage what directories are part of this project","project");
	
	public String description;
	public String[] types;
	
	private AccessType(String description, String... types) {
		this.description = description;
		this.types = types;
	}
	
	public String[] getTypes() {
		return types;
	}
	
	private static LinkedList<AccessType> accessList(AccessType... types) {
		return new LinkedList<AccessType>(Arrays.asList(types));
	}
	
	public static HashMap<AccessType,List<AccessType>> IMPLICATIONS = new HashMap<AccessType,List<AccessType>>();
	static {
		IMPLICATIONS.put(OWNER, accessList(PROJECT_SET_TEMPLATE,DATA_EDITOR,LISTED,VISIBLE,MANAGE_PERMISSIONS,EDITOR,PROJECT_MANAGE_TEMPLATES,PROJECT_FILE_UPLOAD,PROJECT_FILE_DELETE));
		IMPLICATIONS.put(ADMINISTRATOR, accessList(PROJECT_SET_TEMPLATE,DATA_EDITOR,LISTED,VISIBLE,EDITOR,PROJECT_MANAGE_TEMPLATES,PROJECT_FILE_UPLOAD,PROJECT_FILE_DELETE));
		IMPLICATIONS.put(DATA_EDITOR, accessList(EDITOR));
		IMPLICATIONS.put(ROOT, accessList(MANAGE_PERMISSIONS,CREATE_USERS,CREATE_PAPER,DELETE_PROJECT,CREATE_PROJECT));
		
		for (AccessType type : AccessType.values()) {
			if (Arrays.asList(type.types).contains("paper") || Arrays.asList(type.types).contains("project")) {
				if (IMPLICATIONS.containsKey(type)) {
					IMPLICATIONS.get(type).add(VISIBLE);
				} else {
					IMPLICATIONS.put(type, Arrays.asList(VISIBLE));
				}
			}
		}
	}
	
}
