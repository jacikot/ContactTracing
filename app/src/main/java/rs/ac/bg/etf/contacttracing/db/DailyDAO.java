package rs.ac.bg.etf.contacttracing.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface DailyDAO{

    @Insert
    long insert(DailyKey workout);

    @Query("SELECT * from DailyKey ORDER BY date DESC LIMIT 1")
    LiveData<DailyKey> getLatest();

    @Query("SELECT * from DailyKey ORDER BY date DESC LIMIT :n")
    LiveData<DailyKey> getLastNDays(int n);
}
