package com.kevinluu.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by kevinluu on 12/4/15.
 */
public class CrimeListActivity extends SingleFragmentActivity {
    public Fragment createFragment() {
        return new CrimeListFragment();
    }
}
