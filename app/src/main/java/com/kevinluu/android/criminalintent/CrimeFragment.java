package com.kevinluu.android.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;

import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    public static final String EXTRA_CRIME_ID =
            "com.kevinluu.criminalintent.crime_id";

    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_CALL_SUSPECT = 3;
    private static final int REQUEST_PHOTO = 4;


    private Crime mCrime;
    private File mPhotoFile;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private EditText mTitleField;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;

    private ImageButton mPhotoButton;
    private ImageView mPhotoView;


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("asdf", "on pause");
        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        //
        MenuItem mDeleteButton = menu.findItem(R.id.menu_item_delete_crime);
        mDeleteButton.setVisible(true);
        //
        MenuItem mSubtitleVisible = menu.findItem(R.id.menu_item_show_subtitle);
        mSubtitleVisible.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab crimeLab = CrimeLab.get(getActivity());
                returnResult();
                crimeLab.removeCrime(mCrime.getID());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case REQUEST_DATE:
            case REQUEST_TIME:
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                returnResult();
                updateDate();
                break;
            case REQUEST_CONTACT:
                saveSuspect(data);
                break;
            case REQUEST_CALL_SUSPECT:
                callSuspect(data);
                break;
            case REQUEST_PHOTO:
                updatePhoto();
                break;
        }
    }

    private void updatePhoto() {
        if(mPhotoFile == null || !mPhotoFile.exists() ) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
    private void saveSuspect(Intent data) {
        if(data == null) {
            return;
        }
        Uri contactUri = data.getData();
        String[] queryFields = new String[] {
                ContactsContract.Contacts.DISPLAY_NAME
        };
        Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, null, null, null);

        try {
            if(c.getCount() == 0) {
                return;
            }
            c.moveToFirst();
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);
            //
            updateCallSuspectButton();
        } finally {
            c.close();
        }
    }

    private void callSuspect(Intent data) {
        if(data == null) {
            return;
        }
        Uri contactData = data.getData();
        Cursor c = getActivity().getContentResolver().query(contactData, new String[] {
                ContactsContract.CommonDataKinds.Phone.DATA
        }, null, null, null);
        try {
            if(c.getCount() == 0) {
                return;
            }
            c.moveToFirst();
            String number = c.getString(0);
            //
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:"+number));
            startActivity(intent);
        } finally {
            c.close();
        }


    }

    private void updateCallSuspectButton() {
        if(mCrime.getSuspect() != null) {
            mCallSuspectButton.setText(getString(R.string.call_suspect_button_label, mCrime.getSuspect()));
            mCallSuspectButton.setVisibility(Button.VISIBLE);
        } else {
            mCallSuspectButton.setVisibility(Button.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    public void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CRIME_ID, mCrime.getID());
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        //
        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                returnResult();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });
        //
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                returnResult();
            }
        });
        //
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        ViewTreeObserver viewTreeObserver = mPhotoView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhoto();
                //is this normal practice? removing global listeners like this
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        //
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });
        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mTimeButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });
        //
        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setType("text/plain")
                        .getIntent();
                intent = Intent.createChooser(intent, getString(R.string.send_report));
                startActivity(intent);
            }
        });

        //
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }
        updateDate();
        //
        mCallSuspectButton = (Button) v.findViewById(R.id.call_suspect);
        mCallSuspectButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CALL_SUSPECT);
            }
        });
        updateCallSuspectButton();
        //
        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
            mCallSuspectButton.setEnabled(false);
        }
        //
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(mPhotoFile.getAbsolutePath()));
        boolean canTakePhoto = mPhotoFile != null &&
                imageIntent.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if(canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(imageIntent, REQUEST_PHOTO);
            }
        });
        mPhotoView.setOnClickListener(new ImageView.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mPhotoFile.exists()) {
                    FragmentManager manager = getFragmentManager();
                    PhotoViewerFragment photoViewerFragment = PhotoViewerFragment.newInstance(mPhotoFile);
                    photoViewerFragment.show(manager, "view_image_dialog");
                }
            }
        });

        return v;
    }

    private void updateDate() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        mDateButton.setText("Date: "+df.format(mCrime.getDate()));
        SimpleDateFormat df2 = new SimpleDateFormat("h:mm a");
        mTimeButton.setText("Time: "+df2.format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString;
        if(mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, mCrime.getSuspect());
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

}
