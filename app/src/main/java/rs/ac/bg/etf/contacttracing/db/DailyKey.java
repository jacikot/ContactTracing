package rs.ac.bg.etf.contacttracing.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class DailyKey {
    @PrimaryKey(autoGenerate = true)
    int id;
    String dailyKey;
    Date date;

    public DailyKey(String dailyKey, Date date) {
        this.dailyKey = dailyKey;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDailyKey() {
        return dailyKey;
    }

    public void setDailyKey(String dailyKey) {
        this.dailyKey = dailyKey;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
