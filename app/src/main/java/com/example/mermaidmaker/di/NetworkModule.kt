package com.example.mermaidmaker.di


import com.example.mermaidmaker.data.network.GeminiApiService
import com.example.mermaidmaker.data.network.OpenAiApiService
import com.example.mermaidmaker.domain.model.AiProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    
    // HTTP Client
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // OpenAI Retrofit instance
    single<Retrofit>(qualifier = org.koin.core.qualifier.named("openai")) {
        Retrofit.Builder()
            .baseUrl(AiProvider.OPENAI.baseUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Gemini Retrofit instance
    single<Retrofit>(qualifier = org.koin.core.qualifier.named("gemini")) {
        Retrofit.Builder()
            .baseUrl(AiProvider.GEMINI.baseUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API Services
    single<OpenAiApiService> {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("openai")).create(OpenAiApiService::class.java)
    }
    
    single<GeminiApiService> {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("gemini")).create(GeminiApiService::class.java)
    }
}
