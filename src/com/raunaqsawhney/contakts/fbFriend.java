package com.raunaqsawhney.contakts;

public class fbFriend {
	
	private String id; 
	private String name;
	private String url;
	private Boolean isAppUser;
	private String birthday;
	private String current_loc_city;
	private String current_loc_state;
	private String current_loc_country;
	private String current_home_city;
	private String current_home_state;
	private String current_home_country;
	private String username;
	private String coverUrl;
	
	
	
	//Setters
	public void setName(String name) {
	    this.name = name;
	}
	
	public void setID(String id) {
	    this.id = id;
	}
	
	public void setURL(String urlImg) {
		this.url = urlImg;
	}
	
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setIsAppUser(Boolean isAppUser) {
		this.isAppUser = isAppUser;
	}
	
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setCurrentLocCity(String current_loc_city) {
		this.current_loc_city = current_loc_city;
	}
	
	public void setCurrentLocState(String current_loc_state) {
		this.current_loc_state = current_loc_state;
		
	}
	
	public void setCurrentLocCountry(String current_loc_country) {
		this.current_loc_country = current_loc_country;
		
	}
	
	public void setCurrentHomeCity(String current_home_city) {
		this.current_home_city = current_home_city;
		
	}
	
	public void setCurrentHomeState(String current_home_state) {
		this.current_home_state = current_home_state;
		
	}
	
	public void setCurrentHomeCountry(String current_home_country) {
		this.current_home_country = current_home_country;
		
	}
	
	
    // Getters
    public String getName() {
        return name;
    }
    
    public String getID() {
        return id;
    }
    
    public String getURL() {
        return url;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }
    
	public String getUsername() {
		return username;
	}
    
    public Boolean getIsAppUser() {
        return isAppUser;
    }
    
	public String getBirthday() {
		return birthday;
	}

	public String getCurrentLocCity() {
		 return current_loc_city;
	}
	
	public String getCurrentLocState() {
		return current_loc_state;
		
	}
	
	public String getCurrentLocCountry() {
		return current_loc_country;
		
	}
	
	public String getCurrentHomeCity() {
		return current_home_city;
		
	}
	
	public String getCurrentHomeState() {
		return current_home_state;
		
	}
	
	public String getCurrentHomeCountry() {
		return current_home_country;
		
	}
}


