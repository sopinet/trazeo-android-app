package com.sopinet.trazeo.app.gson;

public class EChild {
    public String id;
    public String nick;
    public String date_birth;
    public String visibility;
    public String gender;
    public String selected = "-";

    public boolean isSelected() {
        if(this.selected.equals("true"))
            return true;
        else
            return false;
    }
    public void setSelected(Boolean selected) {
        this.selected = selected.toString();
    }
}