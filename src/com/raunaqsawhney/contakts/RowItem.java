package com.raunaqsawhney.contakts;

public class RowItem {
	
	private int navImageId;
    private String navName;

    public RowItem(int navImageId, String navName) {
        this.navImageId = navImageId;
        this.navName = navName;
    }
 
    public int getNavImageId() {
        return navImageId;
    }
    public void setImageId(int navImageId) {
        this.navImageId = navImageId;
    }
    public String getNavName() {
        return navName;
    }
    public void setNavName(String navName) {
        this.navName = navName;
    }
}
