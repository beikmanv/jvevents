package com.northcoders.jvevents.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.northcoders.jvevents.interceptor.FirebaseAuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitInstance {

    private static final String BASE_URL = "http://10.0.2.2:8085/api/v1/";
    private static Retrofit retrofitInstance;
    private static final Gson gson = new GsonBuilder()
            .setLenient() // Allows parsing of malformed JSON (temporary for debugging)
            .create();

    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            synchronized (RetrofitInstance.class) {
                if (retrofitInstance == null) {
                    retrofitInstance = createRetrofit(null);
                }
            }
        }
        return retrofitInstance;
    }

    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    public static ApiService getApiServiceWithAuth(String idToken) {
        return createRetrofit(idToken).create(ApiService.class);
    }

    private static Retrofit createRetrofit(String idToken) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS); // Logs request and response bodies

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

        if (idToken != null) {
            clientBuilder.addInterceptor(new FirebaseAuthInterceptor(idToken));
        }

        OkHttpClient client = clientBuilder.build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
