package com.sopinet.trazeo.app.gson;


import com.sopinet.android.nethelper.MinimalJSON;

import java.util.ArrayList;

public class Catalog extends MinimalJSON {
    public String position;
    public String title;
    public String description;
    public String link;
    public String company;
    public String points;
    public String created_at;
    public String updated_at;
    public ArrayList<CatalogImageFile> file;
    public String url;
    public String id;
}
