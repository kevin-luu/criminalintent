package com.kevinluu.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

/**
 * Created by kevinluu on 12/4/15.
 */
public class CrimeListFragment extends Fragment {

    private static final int REQUEST_CRIME = 1;
    private static final int NEW_CRIME = 4;
    private RecyclerView mCrimeRecyclerView;
    private LinearLayout mPlaceholderLayout;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private Callbacks mCallbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container,false);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPlaceholderLayout = (LinearLayout) view.findViewById(R.id.no_crime_placeholder_layout);
        Button mNewButton = (Button) mPlaceholderLayout.findViewById(R.id.recycler_view_new_crime);
        mNewButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Crime newCrime = new Crime();
                CrimeLab.get(getActivity()).addCrime(newCrime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), newCrime.getID());
                startActivityForResult(intent, NEW_CRIME);
            }
        });


        updateUI();

        return view;
    }

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }
    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        //toggle subtitle
        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CRIME:
                if(data == null) { return; }
                UUID crimeId = (UUID) data.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
                updateUI();
                mAdapter.notifyItemChanged(mAdapter.updateCrime(crimeId));
                break;
            case NEW_CRIME:
                updateUI();
                break;
        }
    }


    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if(mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        togglePlaceholder();
        updateSubtitle();
    }

    private void togglePlaceholder() {
        if (mAdapter.getItemCount() == 0) {
            mPlaceholderLayout.setVisibility(LinearLayout.VISIBLE);
            mCrimeRecyclerView.setVisibility(LinearLayout.GONE);
        } else {
            mPlaceholderLayout.setVisibility(LinearLayout.GONE);
            mCrimeRecyclerView.setVisibility(LinearLayout.VISIBLE);
        }
    }

    private void updateSubtitle() {
        int crimeCount = mAdapter.getItemCount();

        String subtitle = (mSubtitleVisible) ?
                getResources()
                .getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount)
                : null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{

        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
            mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mCrime.setSolved(isChecked);
                    CrimeLab.get(getActivity()).updateCrime(mCrime);
                    mAdapter.updateCrime(mCrime.getID());
                }
            });
        }

        private void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(crime.getTitle());
            mDateTextView.setText(crime.getDate().toString());
            mSolvedCheckBox.setChecked(crime.isSolved());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        private int getPosition(UUID id) {
            int currentIndex = 0;
            for(Crime crime: mCrimes) {
                if(crime.getID().toString().equals(id.toString())) {
                    return currentIndex;
                }
                currentIndex++;
            }
            return -1;
        }

        public int updateCrime(UUID id) {
            int position = getPosition(id);
            Crime crime = CrimeLab.get(getActivity()).getCrime(id);
            mCrimes.set(position, crime);
            return position;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public void removeItem(UUID id) {
            int currentIndex = 0;
            for(Crime crime: mCrimes) {
                if(crime.getID().toString().equals(id.toString())) {
                    mCrimes.remove(currentIndex);
                    this.notifyItemRemoved(currentIndex);
                    this.notifyItemRangeRemoved(currentIndex, mCrimes.size());
                }
                currentIndex++;
            }
        }
    }
}
