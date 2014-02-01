package com.raunaqsawhney.contakts;

public class fbFriend {
	
	private String id; 
	private String name;
	private String url;
	private String presence;
	
	
	
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
	
	public void setPresence(String presence) {
		this.presence = presence;
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
    
    public String getPresence() {
        return presence;
    }


}


