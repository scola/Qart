package com.chyrta.onboarder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboarderAdapter extends FragmentStatePagerAdapter {

    List<OnboarderPage> pages = new ArrayList<OnboarderPage>();

    public OnboarderAdapter(List<OnboarderPage> pages, FragmentManager fm) {
        super(fm);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int position) {
        return OnboarderFragment.newInstance(pages.get(position));
    }

    @Override
    public int getCount() {
        return pages.size();
    }


}
