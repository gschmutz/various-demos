package com.trivadis.domain;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

public class Tweet implements Serializable {

	private static final long serialVersionUID = 7173965502903960812L;

	private Long id;
	private DateTime createdAt;
	private List<String> hashtags;
	private List<String> mentions;
	private List<String> urls;
	private User user;
	private String language;

	public Tweet() {
	}

	public Tweet(Long id, DateTime createdAt, List<String> hashtags, List<String> mentions,
			List<String> urls, User user, String language) {
		this.id = id;
		this.createdAt = createdAt;
		this.hashtags = hashtags;
		this.mentions = mentions;
		this.urls = urls;
		this.user = user;
		this.language = language;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getId() {
		return id;
	}

	public DateTime getCreatedAt() {
		return createdAt;
	}

	public List<String> getHashtags() {
		return hashtags;
	}

	public List<String> getMentions() {
		return mentions;
	}

	public List<String> getUrls() {
		return urls;
	}

	public User getUser() {
		return user;
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "Tweet [id=" + id + ", createdAt=" + createdAt + ", hashtags=" + hashtags
				+ ", mentions=" + mentions + ", urls=" + urls + ", user=" + user + ", language="
				+ language + "]";
	}
	

}
