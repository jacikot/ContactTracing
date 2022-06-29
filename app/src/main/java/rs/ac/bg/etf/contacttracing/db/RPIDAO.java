package rs.ac.bg.etf.contacttracing.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface RPIDAO {
    @Insert
    long insert(RPIKey key);

    @Query("SELECT * from RPIKey WHERE date>:date")
    LiveData<List<RPIKey>> getAllAfter(Date date);

    @Query("SELECT * from RPIKey WHERE `key`=:key and mac=:mac and date>:date")
    LiveData<RPIKey> getExisting(String key, String mac, Date date);
}
