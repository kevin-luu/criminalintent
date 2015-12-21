package com.kevinluu.android.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String REFRESH_CRIME_POS =
            "com.kevinluu.criminalintent.refresh_crime_pos";

    private Crime mCrime;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private EditText mTitleField;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    public void returnResult() {
        Intent intent = new Intent();
        int position = CrimeLab.get(getActivity()).getPos(mCrime.getID());
        intent.putExtra(REFRESH_CRIME_POS, position);
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    public static int getRefreshPosition(Intent result) {
        return result.getIntExtra(REFRESH_CRIME_POS, -1);
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
                returnResult();
                mCrime.setSolved(isChecked);
            }
        });
        //

        //
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate());
        mDateButton.setEnabled(false);
        //

        return v;
    }


}
