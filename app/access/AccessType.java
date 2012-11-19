package access;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

public enum AccessType {
	ROOT("user"),
	CREATE_PROJECT("user"),
	DELETE_PROJECT("user"),
	CREATE_PAPER("user"),
	
	LISTED("paper","project"),
	VISIBLE("paper","project"),
	OWNER("paper","project"),
	MANAGE_PERMISSIONS("paper","project"),
	EDITOR("paper","project"),
	
	PROJECT_SET_TEMPLATE("project"),
	PROJECT_MANAGE_TEMPLATES("project"),
	PROJECT_MANAGE_DIRECTORIES("project");
	
	private String[] types;
	
	private AccessType(String... types) {
		this.types = types;
	}
	
	public String[] getTypes() {
		return types;
	}
}
