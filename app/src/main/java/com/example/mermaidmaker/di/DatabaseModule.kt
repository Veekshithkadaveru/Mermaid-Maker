package com.example.mermaidmaker.di

import androidx.room.Room
import com.example.mermaidmaker.data.local.MermaidDatabase
import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.local.dao.TemplateDao
import com.example.mermaidmaker.data.local.migration.DatabaseMigrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<MermaidDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MermaidDatabase::class.java,
            MermaidDatabase.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    android.util.Log.d("MermaidDatabase", "Database created successfully")
                }

                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    android.util.Log.d("MermaidDatabase", "Database opened successfully")
                }
            })
            .build()
    }

    single<DiagramDao> { get<MermaidDatabase>().diagramDao() }

    single<TemplateDao> { get<MermaidDatabase>().templateDao() }
}