package com.github.garyparrot.highbrow.module;

import com.github.garyparrot.highbrow.service.UrbanDictionaryService;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class UrbanDictionaryModule {

    @Provides
    OkHttpClient loggingHttpClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }

    @Provides
    Retrofit urbanDictionaryRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl("https://api.urbandictionary.com/v0/")
                .client(loggingHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    UrbanDictionaryService urbanDictionaryService() {
        return urbanDictionaryRetrofitInstance().create(UrbanDictionaryService.class);
    }


}
