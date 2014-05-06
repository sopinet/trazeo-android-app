package com.sopinet.trazeo.app.gson;

public class EChild {
    public String id;
    public String nick;
    public String date_birth;
    public String visibility;
    public String gender;
    boolean selected = false;

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}