package com.raunaqsawhney.contakts;

import java.util.ArrayList;


public class Contact {

	private String name; 
	private String middle_name; 

	private String organization;
	private String job_title;
	private String notes;
	private String relationship;
	private String relationshipType;
	private String nickname;
	
	private ArrayList<String> phoneNumbers; 
    private ArrayList<String> emailIDs; 
    private ArrayList<String> ims;  
    private ArrayList<String> websites;  
    private ArrayList<String> addresses; 
    private ArrayList<String> dates;  


    // storing bitmaps permanently is probably not the best solution
    //private Bitmap photo; // BE VERY CAREFUL HERE, Bitmaps use lots of memory so only keep them in memory as long as needed

    public Contact() {
        phoneNumbers = new ArrayList<String>();
        emailIDs = new ArrayList<String>();
        ims = new ArrayList<String>();
        websites = new ArrayList<String>();
        addresses = new ArrayList<String>();
        dates = new ArrayList<String>();
                
    }

    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setMiddleName(String m_name) {
        this.middle_name = m_name;
    }
    
    public void setNickname(String n_name) {
        this.nickname = n_name;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public void setJobTitle(String job_title) {
        this.job_title = job_title;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public void addPhoneNumer(String number) {
         this.phoneNumbers.add(number);
    }

    public void addEmailID(String emailID) {
         this.emailIDs.add(emailID);
    }
    
    public void addIM(String IM) {
        this.ims.add(IM);
    }
    
    public void addWebsites(String website) {
        this.websites.add(website);
    }
    
    public void addAddresses(String address) {
        this.addresses.add(address);
    }
    
    public void addDates(String date) {
        this.dates.add(date);
    }
   
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getMiddleName() {
        return middle_name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public String getJobTitle() {
        return job_title;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public String getRelationshipType() {
        return relationshipType;
    }
    
    public String getPhoneByIndex(int index) {
        return phoneNumbers.get(index);
    }
    
    public String getEmaiIDByIndex(int index) {
        return emailIDs.get(index);
    }
    
    public String getIMByIndex(int index) {
        return ims.get(index);
    }
    
    public String getWebsiteByIndex(int index) {
        return websites.get(index);
    }
    
    public String getAddressByIndex(int index) {
        return addresses.get(index);
    }
    
    public String getDatesByIndex(int index) {
        return dates.get(index);
  } 
}
