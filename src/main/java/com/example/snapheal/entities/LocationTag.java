package com.example.snapheal.entities;

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

	public LocationTag(Location location, User taggedUser) {
		super();
		this.location = location;
		this.taggedUser = taggedUser;
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
}
