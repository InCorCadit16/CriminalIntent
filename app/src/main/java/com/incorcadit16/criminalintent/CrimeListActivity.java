package com.incorcadit16.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.CallBacks, CrimeFragment.CallBacks {
    @Override
    Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this,crime.getmId());
            startActivity(intent);
        } else {
            Fragment detailFragment = CrimeFragment.newInstance(crime.getmId());

            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container,detailFragment).commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment fragment = (CrimeListFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_container);
        fragment.updateUI();
    }
}
