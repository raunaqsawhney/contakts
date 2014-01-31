package com.raunaqsawhney.contakts;

public class callHistory {
	
	private String name;
	private String number; 
	private String type;
	private String date;
	private String duration;
	
	//Setters
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNumber(String number) {
	    this.number = number;
	}
	
	
	public void setType(String type) {
	    this.type = type;
	}
	
	public void setDate(String date) {
	    this.date = date;
	}
	
	
	public void setDuration(String duration) {
	    this.duration = duration;
	}
	
    // Getters
	public String getName() {
		return name;
	}
	
    public String getNumber() {
        return number;
    }
	
    public String getType() {
        return type;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getDuration() {
        return duration;
    }
}
