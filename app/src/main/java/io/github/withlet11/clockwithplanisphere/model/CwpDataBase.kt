package io.github.withlet11.clockwithplanisphere.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AbstractSkyModel.HipEntry::class,
        AbstractSkyModel.ConstellationLineEntry::class,
        AbstractSkyModel.NorthMilkyWayDotEntry::class,
        AbstractSkyModel.SouthMilkyWayDotEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CwpDataBase : RoomDatabase() {
    abstract fun cwpDao(): CwpDao

    companion object {
        @Volatile
        private var INSTANCE: CwpDataBase? = null

        fun getInstance(context: Context): CwpDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CwpDataBase::class.java,
                    "clockwithplanisphere.db"
                ).createFromAsset("clockwithplanisphere.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build().also {
                        INSTANCE = it
                    }
            }
    }
}
