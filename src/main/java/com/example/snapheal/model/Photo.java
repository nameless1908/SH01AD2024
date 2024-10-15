package com.example.snapheal.model;
import jakarta.persistence.*;

@Entity
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    private String link_photo;
	public Photo(Location location, String link_photo) {
		super();
		this.location = location;
		this.link_photo = link_photo;
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
	public String getLink_photo() {
		return link_photo;
	}
	public void setLink_photo(String link_photo) {
		this.link_photo = link_photo;
	}	
}
