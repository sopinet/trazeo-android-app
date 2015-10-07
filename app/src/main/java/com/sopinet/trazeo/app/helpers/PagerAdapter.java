package com.sopinet.trazeo.app.helpers;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class PagerAdapter extends FragmentPagerAdapter {


    // List of fragments which are going to set in the view pager widget
    List<Fragment> fragments;
    String title1;
    String title2;

    public PagerAdapter(FragmentManager fm, String title1, String title2) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.title1 = title1;
        this.title2 = title2;
    }

    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        String title = null;

        switch (position)
        {
            case 0:
                title = title1;
                break;
            case 1:
                title = title2;
                break;
        }
        return title;
    }

}
