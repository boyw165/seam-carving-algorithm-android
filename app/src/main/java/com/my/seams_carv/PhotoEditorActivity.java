package com.my.seams_carv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.my.seams_carv.view.PhotoView;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PhotoEditorActivity extends AppCompatActivity {

    Uri mPhotoUri;

    PhotoView mPhotoView;
    View mBtnClose;
    View mBtnDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_editor);

        mPhotoUri = getIntent().getData();

        mPhotoView = (PhotoView) findViewById(R.id.photo);
        mPhotoView.addOnLayoutChangeListener(onLayoutChangeToLoadPhoto());

        mBtnClose = findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(onClickClose());

        mBtnDebug = findViewById(R.id.btn_debug);
        mBtnDebug.setOnClickListener(onClickDebug());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private OnClickListener onClickClose() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    private OnClickListener onClickDebug() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                // DO NOTHING.
            }
        };
    }

    private View.OnLayoutChangeListener onLayoutChangeToLoadPhoto() {
        return new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left,
                                       int top,
                                       int right,
                                       int bottom,
                                       int oldLeft,
                                       int oldTop,
                                       int oldRight,
                                       int oldBottom) {
                mPhotoView.setImageURI(mPhotoUri);
            }
        };
    }
}
