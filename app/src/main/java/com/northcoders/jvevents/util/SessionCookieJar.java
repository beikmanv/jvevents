package com.northcoders.jvevents.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class SessionCookieJar implements CookieJar {

    private final List<Cookie> cookieStore = new ArrayList<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Log.d("COOKIE_JAR", "🍪 Saving cookies: " + cookies);
        cookieStore.clear();
        cookieStore.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        Log.d("COOKIE_JAR", "📤 Loading cookies for: " + url);
        return new ArrayList<>(cookieStore);
    }
}
