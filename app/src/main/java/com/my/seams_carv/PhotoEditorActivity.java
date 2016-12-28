package com.my.seams_carv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PhotoEditorActivity extends AppCompatActivity {

    View mBtnClose;
    View mBtnDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_editor);

        mBtnClose = findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(onClickClose());

        mBtnDebug = findViewById(R.id.btn_debug);
        mBtnDebug.setOnClickListener(onClickDebug());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private View.OnClickListener onClickClose() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    private View.OnClickListener onClickDebug() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DO NOTHING.
            }
        };
    }
}
