package jadkowski.dawid.pollutionapp.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import jadkowski.dawid.pollutionapp.R;
import jadkowski.dawid.pollutionapp.fragments.NearInfoFrag;
import jadkowski.dawid.pollutionapp.fragments.RealTimeFrag;
import jadkowski.dawid.pollutionapp.fragments.StatsFrag;

/**
 * Created by dawid on 22/01/2016.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return NearInfoFrag.newInstance();
                case 1:
                    return StatsFrag.newInstance();
                case 2:
                    return RealTimeFrag.newInstance();
                default:
                    return NearInfoFrag.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab1_name);
                case 1:
                    return getString(R.string.tab2_name);
                case 2:
                    return getString(R.string.tab3_name);
                default:
                    return getString(R.string.tab1_name);
            }
        }
    }
}