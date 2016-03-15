package jp.co.crowdworks.cwlibrarystudy;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_screen);

        setupEditor();
        setupCheckboxes();

        final WeatherAPI api = new WeatherAPI(this);
        api.getWeatherOf("Tokyo")
                .onSuccessTask(new Continuation<JSONObject, Task<JSONObject>>() {
                    @Override
                    public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                        JSONObject weatherJSON = task.getResult();
                        String weatherString = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
                        addText("Tokyo:"+weatherString);

                        return api.getWeatherOf("Osaka");
                    }
                }).onSuccessTask(new Continuation<JSONObject, Task<JSONObject>>() {
                    @Override
                    public Task<JSONObject> then(Task<JSONObject> task) throws Exception {
                        JSONObject weatherJSON = task.getResult();
                        String weatherString = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
                        addText("Osaka:"+weatherString);

                        return api.getWeatherOf("Nagoya");
                    }
                }).onSuccess(new Continuation<JSONObject, Object>() {
                    @Override
                    public Object then(Task<JSONObject> task) throws Exception {
                        JSONObject weatherJSON = task.getResult();
                        String weatherString = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
                        addText("Nagoya:"+weatherString);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "ぜんぶとれたよ", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    }
                }).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            Log.e(TAG,"error", task.getError());
                        }
                        return null;
                    }
                });
    }

    public void addText(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView txt = (TextView) findViewById(R.id.txt_weather);
                String base = txt.getText().toString();
                txt.setText(base+"\n"+text);
            }
        });
    }

    private void setupEditor() {
        final WeatherAPI api = new WeatherAPI(this);
        final EditText editor = (EditText) findViewById(R.id.editor_weather);
        final TextView txt = (TextView) findViewById(R.id.txt_weather);

        RxTextView.afterTextChangeEvents(editor)
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .flatMap(new Func1<TextViewAfterTextChangeEvent, Observable<JSONObject>>() {
                    @Override
                    public Observable<JSONObject> call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                        String place = editor.getText().toString();

                        return api.getRxWeatherOf(place);
                    }
                }).subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            JSONObject weatherJSON = jsonObject;
                            String weatherString = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
                            addText("Tokyo:"+weatherString);
                        }
                        catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }
                    }
                });
    }


    private void setupCheckboxes() {
        final CheckBox checkParent = (CheckBox) findViewById(R.id.chk_parent);
        final CheckBox checkChild1 = (CheckBox) findViewById(R.id.chk_child1);
        final CheckBox checkChild2 = (CheckBox) findViewById(R.id.chk_child2);

        RxCompoundButton.checkedChanges(checkParent)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean==true;
                    }
                })
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean checked) {
                        checkChild1.setChecked(checked);
                        checkChild2.setChecked(checked);
                    }
                });


        Observable.combineLatest(RxCompoundButton.checkedChanges(checkChild1), RxCompoundButton.checkedChanges(checkChild2), new Func2<Boolean, Boolean, Pair<Boolean, Boolean>>() {
            @Override
            public Pair<Boolean, Boolean> call(Boolean aBoolean, Boolean aBoolean2) {
                return new Pair<>(aBoolean, aBoolean2);
            }
        }).filter(new Func1<Pair<Boolean, Boolean>, Boolean>() {
            @Override
            public Boolean call(Pair<Boolean, Boolean> booleanBooleanPair) {
                boolean checked1 = booleanBooleanPair.first;
                boolean checked2 = booleanBooleanPair.second;

                return !checked1 || !checked2;
            }
        }).subscribe(new Subscriber<Pair<Boolean, Boolean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Pair<Boolean, Boolean> booleanBooleanPair) {
                boolean checked1 = booleanBooleanPair.first;
                boolean checked2 = booleanBooleanPair.second;

                checkParent.setChecked(checked1 && checked2);
            }
        });

    }
}
