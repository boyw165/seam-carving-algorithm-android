// Copyright (c) 2016-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.seams_carv;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.my.seams_carv.util.ViewUtil;
import com.my.seams_carv.view.IProgressBarView;
import com.my.seams_carv.view.ImagePreviewerDialogFragment;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    final static String TAG = StartActivity.class.getCanonicalName();
    final static int REQ_TAKE_PHOTO = 0;

    Uri mPhotoPath;
    Observable<Void> mShowPhotoTask;

    Toolbar mToolbar;
    View mBtnTakePhoto;
    View mBtnOpenGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // Main buttons.
        mBtnTakePhoto = findViewById(R.id.btn_take_photo);
        mBtnTakePhoto.setOnClickListener(onClickToTakePhoto());
        mBtnOpenGallery = findViewById(R.id.btn_open_gallery);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mShowPhotoTask != null) {
            mShowPhotoTask.publish()
                          .connect();
            mShowPhotoTask = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.start_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_open_photo_editor:
                openPhotoEditor(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case REQ_TAKE_PHOTO:
                onTakePhoto(resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private OnClickListener onClickToTakePhoto() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermAndDispatchTakePhotoIntent();
            }
        };
    }

    /**
     * The callback function is called after taking a photo.
     */
    private void onTakePhoto(int resultCode,
                             Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        final File file = new File(mPhotoPath.getPath());
        final Uri uri = mPhotoPath;
        if (file.exists()) {
            mShowPhotoTask = Observable
                .create(new ObservableOnSubscribe<Void>() {
                    @Override
                    public void subscribe(ObservableEmitter<Void> e) throws Exception {
//                        showPhoto(uri);
                        openPhotoEditor(uri);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
        } else {
            mPhotoPath = null;
            Snackbar.make(findViewById(android.R.id.content),
                          "The picture doesn't exist.",
                          Snackbar.LENGTH_SHORT)
                    .show();
        }
        // TODO: Add the photo to the system MediaContent.
    }

    private void grantPermAndDispatchTakePhotoIntent() {
        if (Build.VERSION.SDK_INT >= 23) {
            RxPermissions
                .getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                         Manifest.permission.CAMERA)
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean granted) {
                        if (granted) {
                            dispatchTakePhotoIntent();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content),
                                          "WRITE_EXTERNAL_STORAGE | CAMERA is not granted.",
                                          Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // DO NOTHING.
                    }

                    @Override
                    public void onComplete() {
                        // DO NOTHING.
                    }
                });
        } else {
            dispatchTakePhotoIntent();
        }
    }

    private void dispatchTakePhotoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException ex) {
                // DO NOTHING.
                Log.d("xyz", ex.getMessage());
            }

            // Continue only if the File was successfully created.
            if (file != null) {
                // Save the path.
                mPhotoPath = Uri.fromFile(file);

                Log.d("xyz", String.format("Saved file=%s", file.getAbsolutePath()));

                // Specify the authorities under which this content provider can
                // be found. Multiple authorities may be supplied by separating
                // them with a semicolon. Authority names should use a Java-style
                // naming convention (such as com.google.provider.MyProvider) in
                // order to avoid conflicts. Typically this name is the same as
                // the class implementation describing the provider's data
                // structure.
                final String authority = getResources().getString(R.string.file_provider_authority);
                final Uri uri = FileProvider.getUriForFile(this,
                                                           authority,
                                                           file);
                Log.d("xyz", String.format("Generated uri=%s", uri));

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent, REQ_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        final String appName = getResources().getString(R.string.app_name);
        final File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/" + appName);
        // Create the missing parents.
        if (dir.mkdirs() || dir.isDirectory()) {
            return File.createTempFile(timeStamp,
                                       ".jpg",
                                       dir);
        } else {
            throw new IOException(String.format("%s is not present.",
                                                dir.getAbsolutePath()));
        }
    }

    private void showPhoto(final Uri uri) {
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .remove(prev)
                .commit();
        }

        ImagePreviewerDialogFragment
            .newInstance(uri)
            .show(getSupportFragmentManager(), "dialog");
    }

    private void openPhotoEditor(Uri uri) {
        startActivity(new Intent(StartActivity.this,
                                 PhotoEditorActivity.class));
    }
}
