package io.gab.challenge.logreader.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name = "EVENT")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String eventID;
	private Long duration;
	private String type;
	private String host;
	private Boolean alert;
	
	@Transient
	private Long eventStartTime;
	@Transient
	private Long eventEndTime;
	
	public Event() {}
	
	public Event(String eventID) {
		this.eventID = eventID;
	}
	
	public String getEventID() {
		return eventID;
	}
	
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}

	public Boolean getAlert() {
		return alert;
	}
	
	public void setAlert(Boolean alert) {
		this.alert = alert;
	}

	public Long getEventStartTime() {
		return eventStartTime;
	}

	public void setEventStartTime(Long eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	
	public Long getEventEndTime() {
		return eventEndTime;
	}

	public void setEventEndTime(Long eventEndTime) {
		this.eventEndTime = eventEndTime;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null) {
			if(obj == this) return true;
			if(!(obj instanceof Event)) return false;
			
			Event event = (Event) obj;
			return ((Event) obj).getEventID() == eventID;
		}
		
		return false;
	}
}
