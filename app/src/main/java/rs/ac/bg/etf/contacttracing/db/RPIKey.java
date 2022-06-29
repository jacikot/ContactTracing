package rs.ac.bg.etf.contacttracing.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class RPIKey {
    @PrimaryKey(autoGenerate = true)
    int id;
    String key;
    String mac;
    Date date;

    public RPIKey(String key, String mac, Date date) {
        this.key = key;
        this.mac = mac;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
