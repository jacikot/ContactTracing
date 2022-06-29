package rs.ac.bg.etf.contacttracing.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import rs.ac.bg.etf.contacttracing.BluetoothService;

@TypeConverters(value = {DateConverter.class})
@Database(entities = {DailyKey.class, RPIKey.class}, version = 1, exportSchema = false)
abstract public class ContactTracingDatabase extends RoomDatabase {

    public abstract DailyDAO getDao();
    public abstract RPIDAO getRPIDao();

    private static ContactTracingDatabase instance=null;
    private static final String DBname="contact-tracing";

    public static ContactTracingDatabase getInstance(BluetoothService service){
        if(instance==null){
            //double check u javi pattern!!!
            synchronized (ContactTracingDatabase.class){
                if(instance==null){
                    instance= Room.databaseBuilder(service.getApplicationContext(), ContactTracingDatabase.class,DBname).allowMainThreadQueries().build();

                }
            }

        }
        return instance;
    }

}