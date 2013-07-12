// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Podbase extends PermissionedModel {
	public Podbase() {
		super();
	}
	
	public static Podbase getInstance() {
		List<Podbase> list = Podbase.findAll();
		if (list.isEmpty()) return null;
		return list.get(0);
	}
}
