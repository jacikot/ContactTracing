package rs.ac.bg.etf.contacttracing.rest;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;


//model podataka koji se vracaju
public class DiagnosedPositive {

    public Coordinates coord;
    public List<Weather> weather;
    public String base;
    public Main main;
    public double visibility;
    public Wind wind;
    public Clouds clouds;
    public long dt;
    public Sys sys;
    public long id;
    public String name;
    public long cod;

    public static class Coordinates {
        @SerializedName("lat")
        public double latitude;
        @SerializedName("lon")
        public double longitude;
    }

    public static class Weather {
        public long id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Wind {
        public double speed;
        public double deg;
    }

    public static class Clouds {
        public double all;
    }

    public static class Sys {
        public long type;
        public long id;
        public String country;
        public long sunrise;
        public long sunset;
    }

    public static class Main {
        public double temp;
        public double feels_like;
        public double pressure;
        public double humidity;
        public double temp_min;
        public double temp_max;
    }

    @NonNull
    @Override
    public String toString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(this);
    }
}
