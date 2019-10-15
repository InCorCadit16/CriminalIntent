package com.incorcadit16.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {
    private final int REQUEST_CRIME = 1;
    private final String SUBTITLE_VISIBLE = "subtitle";
    private boolean mSubtitleVisible;

    private Button mNewCrimeButton;
    private RecyclerView mRecyclerView;
    private CrimeAdapter mAdapter;

    // Интерфейс используется в методах onAttach и onDetach
    // Активность-хост обязательно должна реализовать интерфейс CallBacks
    private CallBacks callBacks;

    public interface CallBacks {
        void onCrimeSelected(Crime crime);
        void onCrimeUpdated(Crime crime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mNewCrimeButton = (Button) v.findViewById(R.id.new_crime);
        if (CrimeLab.get(getActivity()).getCrimes().size() < 1) {
            mNewCrimeButton.setVisibility(View.VISIBLE);
            mNewCrimeButton.setOnClickListener((v1 -> {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getmId());
                startActivity(intent);
            }));
        } else {
            mNewCrimeButton.setVisibility(View.GONE);
        }

        mRecyclerView = (RecyclerView) v.findViewById(R.id.crime_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SUBTITLE_VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBacks = (CallBacks) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        updateSubtitle();
        mNewCrimeButton = (Button) getActivity().findViewById(R.id.new_crime);
        if (CrimeLab.get(getActivity()).getCrimes().size() < 1) {
            mNewCrimeButton.setVisibility(View.VISIBLE);
            mNewCrimeButton.setOnClickListener((v1 -> {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(),crime.getmId());
                startActivity(intent);
            }));
        } else {
            mNewCrimeButton.setVisibility(View.GONE);
        }
    }

    void updateUI() {
        CrimeLab lab = CrimeLab.get(getActivity());
        List<Crime> crimes = lab.getCrimes();
        if (mAdapter  == null) {
            mAdapter = new CrimeAdapter(crimes);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder {
        private Crime mCrime;
        private TextView mTitleView, mDateView;
        private ImageView mCrimeSolved;

        CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener((v) -> {
                callBacks.onCrimeSelected(mCrime);
                });

            mTitleView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateView = (TextView) itemView.findViewById(R.id.crime_date);
            mCrimeSolved = (ImageView) itemView.findViewById(R.id.crime_solved);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleView.setText(mCrime.getmTitle());
            SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.ENGLISH);
            mDateView.setText(format.format(mCrime.getmDate()));
            mCrimeSolved.setVisibility(mCrime.ismSolved()? View.VISIBLE:View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem showSubtitle = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        } else {
            showSubtitle.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                updateUI();
                callBacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        int count = CrimeLab.get(getActivity()).getCrimes().size();
        String subtitle = getString(R.string.subtitle_format,count);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        CrimeAdapter (List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }


        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            holder.bind(mCrimes.get(position));
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CRIME) {
            // Обработка результата
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callBacks = null;
    }
}
