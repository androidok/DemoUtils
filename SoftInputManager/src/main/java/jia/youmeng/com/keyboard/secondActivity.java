package jia.youmeng.com.keyboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class secondActivity extends AppCompatActivity {

    EditText button1;
    EditText button2;
    EditText button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        button1= (EditText) findViewById(R.id.editText);
        button2= (EditText) findViewById(R.id.editText2);
        button3= (EditText) findViewById(R.id.editText3);

    }
}
