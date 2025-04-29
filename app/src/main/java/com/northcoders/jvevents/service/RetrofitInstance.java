package com.northcoders.jvevents.service;

import com.northcoders.jvevents.util.SessionCookieJar;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.net.CookieManager;
import java.net.CookiePolicy;
import okhttp3.JavaNetCookieJar;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitInstance {

    private static final String Base_URL = "http://10.0.2.2:8085/api/v1/";
    private static ApiService service;

    public static ApiService getService() {
        if (service == null) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .cookieJar(new SessionCookieJar())
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Base_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // 🔥 Add this before Gson
                    .addConverterFactory(GsonConverterFactory.create())    // Keep Gson after Scalars
                    .build();

            service = retrofit.create(ApiService.class);
        }

        return service;
    }
}
