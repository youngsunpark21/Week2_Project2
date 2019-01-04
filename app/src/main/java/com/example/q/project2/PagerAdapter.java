package com.example.q.project2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public PagerAdapter(FragmentManager fm, int numOfTabsGiven) {
        super(fm);
        this.numOfTabs = numOfTabsGiven;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                Fragment1 frag1 = new Fragment1();
                return frag1;
            case 1:
                Fragment frag2 = new Fragment2();
                return frag2;
            case 2:
                Fragment frag3 = new Fragment3();
                return frag3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
