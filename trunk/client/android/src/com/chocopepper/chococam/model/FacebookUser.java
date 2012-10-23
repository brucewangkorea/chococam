package com.chocopepper.chococam.model;

public class FacebookUser {
	public String picture;
	public String name;
	public String id;
	
	public FacebookUser(String id, String name, String picture) {
		super();
		this.picture = picture;
		this.name = name;
		this.id = id;
	}
	
	// 2012-10-09 brucewang
	// chococam 서버상의 사용자 id
	public String choco_user_id;
}
