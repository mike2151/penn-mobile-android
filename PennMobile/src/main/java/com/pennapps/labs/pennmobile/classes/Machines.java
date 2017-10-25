package com.pennapps.labs.pennmobile.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Machines {

    @SerializedName("Dryers")
    @Expose
    private Dryers dryers;
    @SerializedName("Washers")
    @Expose
    private Washers washers;

    public Dryers getDryers() {
        return dryers;
    }

    public void setDryers(Dryers dryers) {
        this.dryers = dryers;
    }

    public Washers getWashers() {
        return washers;
    }

    public void setWashers(Washers washers) {
        this.washers = washers;
    }

}