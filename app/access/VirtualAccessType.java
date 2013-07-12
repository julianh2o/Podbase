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

public class VirtualAccessType {
	public AccessType type;
	public List<AccessType> impliedBy;
	
	public VirtualAccessType(AccessType type) {
		this(type,null);
	}
	
	public VirtualAccessType(AccessType type, AccessType impliedBy) {
		this.type = type;
		this.impliedBy = new LinkedList<AccessType>();
		if (impliedBy != null) addImplication(impliedBy);
	}
	
	public String name() {
		return type.name();
	}
	
	public String getDescription() {
		return type.description;
	}
	
	public void addImplication(AccessType implication) {
		this.impliedBy.add(implication);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof VirtualAccessType)) return false;
		VirtualAccessType vat = (VirtualAccessType)o;
		return this.type == vat.type;
	}
	
	public int hashCode() {
		return type.hashCode();
	}
}
