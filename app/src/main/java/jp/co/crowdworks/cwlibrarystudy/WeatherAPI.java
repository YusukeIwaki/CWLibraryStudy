package jp.co.crowdworks.cwlibrarystudy;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import rx.Observable;

import bolts.Task;
import bolts.TaskCompletionSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscriber;

public class WeatherAPI {
    private final Context mContext;
    public WeatherAPI(Context context) {
        mContext = context;
    }

    public static class HTTPError extends RuntimeException {
        private final Response mResponse;
        public HTTPError(Response res) {
            mResponse = res;
        }
    }

    public Task<JSONObject> getWeatherOf(String place) {
        final TaskCompletionSource<JSONObject> _task = new TaskCompletionSource<>();

        Request req = new Request.Builder()
                .get()
                .url("http://api.openweathermap.org/data/2.5/weather?q="+place+",jp&appid=b1b15e88fa797225412429c1c50c122a")
                .build();

        OkHttpHelper.getHttpClient().newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                _task.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        _task.setResult(new JSONObject(response.body().string()));
                    } catch (JSONException e) {
                        _task.setError(e);
                    }
                }
                else {
                    _task.setError(new HTTPError(response));
                }
            }
        });


        return _task.getTask();
    }

    public Observable<JSONObject> getRxWeatherOf(final String place) {
        Observable<JSONObject> observable = Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(final Subscriber<? super JSONObject> subscriber) {
                Request req = new Request.Builder()
                        .get()
                        .url("http://api.openweathermap.org/data/2.5/weather?q="+place+",jp&appid=b1b15e88fa797225412429c1c50c122a")
                        .build();

                OkHttpHelper.getHttpClient().newCall(req).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                subscriber.onNext(new JSONObject(response.body().string()));
                                subscriber.onCompleted();
                            } catch (JSONException e) {
                                subscriber.onError(e);
                            }
                        }
                        else {
                            subscriber.onError(new HTTPError(response));
                        }
                    }
                });

            }
        });
        return observable;
    }
}
