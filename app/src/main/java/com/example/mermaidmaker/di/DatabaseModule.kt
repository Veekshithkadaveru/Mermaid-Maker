package com.example.mermaidmaker.di

import androidx.room.Room
import com.example.mermaidmaker.data.local.MermaidDatabase
import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.local.dao.TemplateDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<MermaidDatabase> {
        Room.databaseBuilder(
            androidContext(),
            MermaidDatabase::class.java,
            MermaidDatabase.DATABASE_NAME
        ).build()
    }

    single<DiagramDao> { get<MermaidDatabase>().diagramDao() }
    
    single<TemplateDao> { get<MermaidDatabase>().templateDao() }
}