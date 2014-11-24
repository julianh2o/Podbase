// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package access;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

public enum AccessType {
	//General permissions
	CREATE_PROJECT("Allows user to create new projects",new String[] {"user"}),
	DELETE_PROJECT("Allows user to delete projects",new String[] {"user"}),
	
	CREATE_PAPER("Allows user to create papers",new String[] {"user"}),
	DELETE_PAPER("Allows user to delete papers",new String[] {"user"}),
	
	CREATE_USERS("Allows user to create users",new String[] {"user"}),

	//Paper and Project permissions
	PARTICIPANT("User participates in this object",new String[] {"paper","project"}),
	LISTED("This object is visible on the home page",new String[] {"paper","project"}),
	VIEW_VISIBLE_IMAGES("The user can see visible images",new String[] {"project"}),
	VIEW_ALL_IMAGES("The user can see all images",new String[] {"project"}),
	NO_WATERMARK("The user can view images without the watermark",new String[] {"paper","project"}),

	SET_DATA_MODE("The user can toggle the data mode for a project",new String[] {"paper","project"}),
	SET_VISIBLE("The user can toggle visibility for images",new String[] {"paper","project"}),
	EDIT_DATA_METADATA("The user can edit data-mode metadata",new String[] {"paper","project"}),
	EDIT_ANALYSIS_METADATA("The user can edit analysis-mode metadata",new String[] {"paper","project"}),

	MANAGE_PERMISSIONS("The user is allowed to manage permissions for this entity",new String[] {"paper","project","user"}),
	
	FILE_UPLOAD("The user can upload files",new String[] {"project"}),
	FILE_DELETE("The user can delete files",new String[] {"project"}),

	SET_TEMPLATE("The user can set the template used by a directory",new String[] {"project"}),
	VIEW_TEMPLATES("The user can view templates",new String[] {"project"}),
	CREATE_TEMPLATES("The user can edit templates",new String[] {"project"}),
	EDIT_TEMPLATES("The user can edit templates",new String[] {"project"}),
	DELETE_TEMPLATES("The user can delete templates",new String[] {"project"}),

	//Aggregate Permissions
	OWNER("The user is considered an owner of this entity and can perform any actions",new String[] {"paper","project"},true),
	TEMPLATE_EDITOR("The user can manage templates",new String[] {"project"},true),
	MANAGER("The user can fully manage the project",new String[] {"project"},true);
	
	public String description;
	public String[] types;
	public boolean aggregate;
	
	private AccessType(String description, String[] types) {
		this(description,types, false);
	}
	
	private AccessType(String description, String[] types, boolean aggregate) {
		this.description = description;
		this.types = types;
		this.aggregate = aggregate;
	}
	
	public String[] getTypes() {
		return types;
	}
	
	private static LinkedList<AccessType> accessList(AccessType... types) {
		return new LinkedList<AccessType>(Arrays.asList(types));
	}
	
	public static HashMap<AccessType,List<AccessType>> IMPLICATIONS = new HashMap<AccessType,List<AccessType>>();
	static {
		//Grants the user access to all the template editing files
		IMPLICATIONS.put(TEMPLATE_EDITOR, accessList(
			VIEW_TEMPLATES,
			CREATE_TEMPLATES,
			EDIT_TEMPLATES,
			DELETE_TEMPLATES
		));
		
		//Grants the user access to all of paper/project functionality
		IMPLICATIONS.put(OWNER, accessList(
			PARTICIPANT,
			LISTED,
			VIEW_VISIBLE_IMAGES,
			VIEW_ALL_IMAGES,
			NO_WATERMARK,
			SET_DATA_MODE,
			EDIT_DATA_METADATA,
			EDIT_ANALYSIS_METADATA,
			MANAGE_PERMISSIONS,
			SET_TEMPLATE,
			VIEW_TEMPLATES,
			EDIT_TEMPLATES,
			CREATE_TEMPLATES,
			DELETE_TEMPLATES,
			FILE_UPLOAD,
			FILE_DELETE,
			SET_VISIBLE
		));
		
		IMPLICATIONS.put(MANAGER, accessList(
			PARTICIPANT,
			LISTED,
			VIEW_VISIBLE_IMAGES,
			VIEW_ALL_IMAGES,
			EDIT_DATA_METADATA,
			EDIT_ANALYSIS_METADATA,
			SET_VISIBLE,
			SET_TEMPLATE,
			SET_DATA_MODE,
			NO_WATERMARK,
			FILE_UPLOAD,
			FILE_DELETE
		));
		
//		for (AccessType type : AccessType.values()) {
//			if (Arrays.asList(type.types).contains("paper") || Arrays.asList(type.types).contains("project")) {
//				if (IMPLICATIONS.containsKey(type)) {
//					IMPLICATIONS.get(type).add(VISIBLE);
//				} else {
//					IMPLICATIONS.put(type, Arrays.asList(VISIBLE));
//				}
//			}
//		}
	}
	
}
