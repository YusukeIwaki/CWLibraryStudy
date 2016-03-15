package jp.co.crowdworks.cwlibrarystudy;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

public class OkHttpHelper {
    private static OkHttpClient sInstance;
    public static final OkHttpClient getHttpClient() {
        if (sInstance==null) {
            sInstance = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
        }
        return sInstance;
    }
}
