// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package models;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang.RandomStringUtils;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.*;
import play.libs.Codec;

@Entity
public class Activation extends TimestampModel {
	public String activationCode;
	
	public Date expirationDate;
	
	@OneToOne(mappedBy="activation")
	@GsonTransient
	public User user;
	
	public static Activation generateActivationCode(User u, int validForDays) {
		Activation a = new Activation();
		a.user = u;
		u.activation = a;
		a.activationCode = RandomStringUtils.randomAlphanumeric(40);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, validForDays);
		
		a.expirationDate = c.getTime();
		
		a.save();
		u.save();
		
		return a;
	}
	
	public String toString() {
		return "Activation for "+user.email+" "+activationCode;
	}
	
}