package models;

import java.util.*;
import javax.persistence.*;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

@Entity
public class Permission extends TimestampModel {
	@OneToOne
	@GsonTransient
	public User user;
	
	
}