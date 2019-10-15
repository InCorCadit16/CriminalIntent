package com.incorcadit16.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.CallBacks {
    private static final String ID = "id";
    private ViewPager mViewPager;
    private List<Crime> mCrimes;

    @Override
    public void onCrimeUpdated(Crime crime) {

    }

    public static Intent newIntent(Context context, UUID crimeId) {
        Intent intent = new Intent(context, CrimePagerActivity.class);
        intent.putExtra(ID,crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra(ID);

        mViewPager = (ViewPager) findViewById(R.id.crime_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fm = getSupportFragmentManager();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Crime mCrime = mCrimes.get(position);
                return CrimeFragment.newInstance(mCrime.getmId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for (Crime mCrime: mCrimes) {
            if (mCrime.getmId().equals(crimeId)) {
                mViewPager.setCurrentItem(mCrimes.indexOf(mCrime));
                break;
            }
        }
    }
}
