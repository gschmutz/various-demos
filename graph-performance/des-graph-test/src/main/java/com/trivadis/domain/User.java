package com.trivadis.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;


public class User implements Serializable {
	private static final long serialVersionUID = 1599726244292490002L;

	private Long id;
	private String idAsString;
	private DateTime createdAt;
	private String language;
	private String screenName;
	private Boolean verified;

	public User() {

	}

	public User(Long id, String idAsString, DateTime createdAt, String language, String screenName, Boolean verified) {
		super();
		this.id = id;
		this.idAsString = idAsString;
		this.createdAt = createdAt;
		this.language = language;
		this.screenName = screenName;
		this.verified = verified;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getId() {
		return id;
	}

	public String getIdAsString() {
		return idAsString;
	}

	public DateTime getCreatedAt() {
		return createdAt;
	}

	public String getLanguage() {
		return language;
	}

	public String getScreenName() {
		return screenName;
	}

	public Boolean getVerified() {
		return verified;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", idAsString=" + idAsString + ", createdAt=" + createdAt + ", language=" + language
				+ ", screenName=" + screenName + ", verified=" + verified + "]";
	}

}
