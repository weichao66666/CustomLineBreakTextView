package io.weichao.customlinebreaktextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.weichao.customlinebreaktextview.widget.CustomLineBreakTextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomLineBreakTextView clbTv = findViewById(R.id.custom_line_break_textview);
        clbTv.setText("byexwayyynhhreuwbrewy " +
                "T-shirtT-shirtT-shirtT-shirt " +
                "T-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirt " +
                "T-shirt " +
                "T-shirtT-shirtT-shirtT-shirtT-shirtT-shirt " +
                "T-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirt " +
                "T-shirtT-shirt " +
                "T-shirtT-shirt " +
                "T-shirtT-shirtT-shirtT-shirt " +
                "T-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirt " +
                "T-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirtT-shirt " +
                "byexwayyynhhreuwbrewy");
//        clbTv.setText("byexwayyynhhreuwbrewy " +
//                "T-shirtT-shirtT-shirtT-shirt");
    }
}
