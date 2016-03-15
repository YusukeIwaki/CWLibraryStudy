package jp.co.crowdworks.cwlibrarystudy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_screen);

        Request req = new Request.Builder()
                .get()
                .url("http://api.openweathermap.org/data/2.5/weather?q=Tokyo,jp&appid=b1b15e88fa797225412429c1c50c122a")
                .build();

        OkHttpHelper.getHttpClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "error", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    Log.d(TAG, "response="+res);
                }
                else {
                    Log.w(TAG, "http error: code="+response.code());
                }
            }
        });
    }
}
