package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Project extends TimestampModel {
	public String name;

	public Project(String name) {
		super();
		this.name = name;
	}
}
