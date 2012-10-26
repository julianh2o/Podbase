package access;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

public enum AccessType {
	ROOT,
	CREATE_PROJECT,
	DELETE_PROJECT,
	
	PROJECT_LISTED,
	PROJECT_VISIBLE,
	PROJECT_OWNER,
	PROJECT_EDIT_METADATA,
	PROJECT_SET_TEMPLATE,
	PROJECT_MANAGE_PERMISSIONS,
	PROJECT_MANAGE_TEMPLATES,
	PROJECT_MANAGE_DIRECTORIES,
	
	PAPER_LISTED,
	PAPER_VISIBLE,
	PAPER_OWNER,
}
