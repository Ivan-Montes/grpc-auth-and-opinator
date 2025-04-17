package dev.ime.domain.model;

import java.util.Objects;
import java.util.UUID;

public class UserApp {

	private UUID userAppId;
	private String email;
	private String name;
	private String lastname;
	
	public UserApp(UUID userAppId, String email, String name, String lastname) {
		super();
		this.userAppId = userAppId;
		this.email = email;
		this.name = name;
		this.lastname = lastname;
	}
	
	public UUID getUserAppId() {
		return userAppId;
	}
	public void setUserAppId(UUID userAppId) {
		this.userAppId = userAppId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(email, lastname, name, userAppId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserApp other = (UserApp) obj;
		return Objects.equals(email, other.email) && Objects.equals(lastname, other.lastname)
				&& Objects.equals(name, other.name) && Objects.equals(userAppId, other.userAppId);
	}
	
	@Override
	public String toString() {
		return "UserApp [userAppId=" + userAppId + ", email=" + email + ", name=" + name + ", lastname=" + lastname
				+ "]";
	}

}
