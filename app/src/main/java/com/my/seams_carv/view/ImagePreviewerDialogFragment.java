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

package com.my.seams_carv.view;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.my.seams_carv.R;

public class ImagePreviewerDialogFragment extends DialogFragment {

    private static final String PREFIX = ImagePreviewerDialogFragment.class.getCanonicalName();
    public static final String EXTRA_URI = PREFIX + ".extra_uri";

    public static ImagePreviewerDialogFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_URI, uri);

        ImagePreviewerDialogFragment fragment = new ImagePreviewerDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_image_previewer, container, false);

        SimpleDraweeView imageView = (SimpleDraweeView) layout.findViewById(R.id.image);
        TextView imageUriView = (TextView) layout.findViewById(R.id.image_uri);

        Uri uri = getArguments().getParcelable(EXTRA_URI);
        if (uri != null) {
            imageView.setImageURI(uri);
            imageUriView.setText(uri.getPath());
        }

        return layout;
    }
}
