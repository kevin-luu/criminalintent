package com.kevinluu.android.criminalintent;

import java.util.Date;
import java.text.DateFormat;
import java.util.UUID;

/**
 * Created by kevinluu on 12/4/15.
 */
public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getID() {
        return mId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Date getDate() {
        return mDate;
    }

    public int getPosition() {
        return CrimeLab.get(null).getPos(getID());
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }


}
