package jia.youmeng.com.keyboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Scroll";
    private EditText mEdit;
    private Button mButton;
    private Button mButton1;
    private ScrollView mScrollView;
    private Handler mHandler = new Handler();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollView = (ScrollView) findViewById(R.id.scroll);
        mEdit = (EditText) findViewById(R.id.edit);
        mButton = (Button) findViewById(R.id.button);
        mButton1 = (Button) findViewById(R.id.button1);
        mEdit.setOnClickListener(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mButton1.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit:
                //这里必须要给一个延迟，如果不加延迟则没有效果。我现在还没想明白为什么
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //将ScrollView滚动到底
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 100);
                break;
            case R.id.button1:
                Intent  intent=new Intent(this,secondActivity.class);
                startActivity(intent);


        }


    }
}
