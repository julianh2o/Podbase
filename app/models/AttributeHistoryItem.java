package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.modules.search.Indexed;

@Entity
@Indexed
public class AttributeHistoryItem extends TimestampModel {
	@ManyToOne
	@GsonTransient
	ImageAttribute attribute;
	
	@OneToOne
	User user;

	@Column(columnDefinition="text")
	String previousValue;
	
	@Column(columnDefinition="text")
	String newValue;
	
	@Column(columnDefinition="text")
	String note;
	
	public AttributeHistoryItem(ImageAttribute attribute, User user, String previousValue, String newValue, String note) {
		this.attribute = attribute;
		this.user = user;
		this.previousValue = previousValue;
		this.newValue = newValue;
		this.note = note;
	}
}
