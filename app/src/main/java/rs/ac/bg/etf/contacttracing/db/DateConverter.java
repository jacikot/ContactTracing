package rs.ac.bg.etf.contacttracing.db;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public long DateToTimestamp(Date d){
        return d.getTime();
    }

    @TypeConverter
    public Date TimestampToDate(long timestamp){
        return new Date(timestamp);
    }
}
