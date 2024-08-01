package com.example.smapp.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.smapp.Fragment.Notification2Fragment;
import com.example.smapp.Fragment.RequestFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if(position==1)return new RequestFragment();
        else  return new Notification2Fragment();

    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){

        String title = null;

        if(position==0){
            title= "NOTIFICATION";
        }
        else if(position==1){
            title="REQUEST";
        }
        return title;

    }
}
