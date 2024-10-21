package com.example.snapheal.entities;

import jakarta.persistence.*;

@Entity
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Enumerated(EnumType.STRING)  // Sử dụng EnumType.STRING để lưu trữ giá trị enum dưới dạng chuỗi
    private FriendStatus status;
    
	public FriendRequest(User requester, User receiver, FriendStatus status) {
		super();
		this.requester = requester;
		this.receiver = receiver;
		this.status = status;
	}
	public FriendRequest() {}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getRequester() {
		return requester;
	}
	public void setRequester(User requester) {
		this.requester = requester;
	}
	public User getReceiver() {
		return receiver;
	}
	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}
	public FriendStatus getStatus() {
		return status;
	}
	public void setStatus(FriendStatus status) {
		this.status = status;
	}}
