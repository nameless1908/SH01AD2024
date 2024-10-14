package com.example.snapheal.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class LocationTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "tagged_user_id")
    private User taggedUser;

    private LocalDateTime createdAt;

	public LocationTag(Long id, Location location, User taggedUser, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.location = location;
		this.taggedUser = taggedUser;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public User getTaggedUser() {
		return taggedUser;
	}

	public void setTaggedUser(User taggedUser) {
		this.taggedUser = taggedUser;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
