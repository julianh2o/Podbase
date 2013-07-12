// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.util.*;
import javax.persistence.*;

import access.AccessType;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

@Entity
public class Permission extends TimestampModel {
	@OneToOne
	@GsonTransient
	public User user;
	
	@OneToOne
	@GsonTransient
	public PermissionedModel model;
	
	public AccessType access;
	
	public Permission(User user, PermissionedModel model, AccessType access) {
		super();
		this.user = user;
		this.model = model;
		this.access = access;
	}
}