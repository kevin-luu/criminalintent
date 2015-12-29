package com.kevinluu.android.criminalintent;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by kevinluu on 12/28/15.
 */
public class PhotoViewerFragment extends DialogFragment {

    private static final String ARG_IMAGE = "image";

    private ImageView mImageView;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_view_image, null);
        File image = (File) getArguments().getSerializable(ARG_IMAGE);
        mImageView = (ImageView) v.findViewById(R.id.expanded_image_view);
        mImageView.setImageBitmap(PictureUtils.getScaledBitmap(image.getPath(), getActivity()));
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setNegativeButton(android.R.string.ok, null)
                .create();
    }

    public static PhotoViewerFragment newInstance(File image) {
        Bundle args = new Bundle();

        args.putSerializable(PhotoViewerFragment.ARG_IMAGE, image);

        PhotoViewerFragment photoViewerFragment = new PhotoViewerFragment();
        photoViewerFragment.setArguments(args);
        return photoViewerFragment;

    }

}
