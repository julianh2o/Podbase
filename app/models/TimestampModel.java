// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;

import play.db.jpa.Model;

@MappedSuperclass
public abstract class TimestampModel extends Model {
	public Date created;
	public Date modified;
	
	public TimestampModel() {
		this.created = new Date();
		this.modified = new Date();
	}
	
	@PreUpdate
	public void updateTimestamp() {
		this.modified = new Date();
	}
}
