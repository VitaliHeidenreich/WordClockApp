package com.domain.no.wordclock;

public class EspDevice {

    EspDevice(String name, String adresse){
        Name = name;
        Adresse = adresse;
    }

    private String Name;
    private String Adresse;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAdresse() {
        return Adresse;
    }

    public void setAdresse(String adresse) {
        Adresse = adresse;
    }
}
