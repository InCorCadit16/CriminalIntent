package com.incorcadit16.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    Crime mCrime;
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final String DIALOG_DATE = "DialogDate";
    private File mPhotoFile;
    private EditText mCrimeTitle;
    private Button mCrimeButton, mSuspectButton, mReportButton;
    private CheckBox mSolvedCheckBox;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;
    private CallBacks callBacks;

    public interface CallBacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID) getArguments().getSerializable("ID");
        mCrime = CrimeLab.get(getContext()).getCrime(id);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBacks = (CallBacks) context;
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    public static CrimeFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable("ID", id);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mCrimeTitle = (EditText) v.findViewById(R.id.crime_title);
        mCrimeTitle.setText(mCrime.getmTitle());
        mCrimeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setmTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCrimeButton = (Button) v.findViewById(R.id.crime_date);
        UpdateDate();
        mCrimeButton.setOnClickListener((view) -> {
            DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getmDate());
            dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
            dialog.show(getFragmentManager(),DIALOG_DATE);
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.ismSolved());
        mSolvedCheckBox.setOnCheckedChangeListener((buttonView,isChecked) -> {
                mCrime.setmSolved(isChecked);
                updateCrime();
            });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener((view) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
            Intent i = Intent.createChooser(intent,getString(R.string.send_report));
            startActivity(i);
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener((view) -> {
            startActivityForResult(pickContact,REQUEST_CONTACT);
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);

        final Intent imageCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Проверяем если есть подходящее прилодение чтобы сделать фото
        boolean canTakePhoto = packageManager.resolveActivity(imageCapture,PackageManager.MATCH_DEFAULT_ONLY) != null &&
                mPhotoFile != null;

        // В зависимости от этого кнопка может быть включена или выключена
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener((v1) -> {
            // Достаём URI файла
            Uri uri = FileProvider.getUriForFile(getActivity(),"com.incorcadit16.criminalintent.fileprovider",mPhotoFile);
            imageCapture.putExtra(MediaStore.EXTRA_OUTPUT,uri);

            // Даём разрешение камере на запись файлов в наше хранилище
            List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(imageCapture,PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo activity : cameraActivities) {
                getActivity().grantUriPermission(activity.activityInfo.packageName,uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            startActivityForResult(imageCapture,REQUEST_PHOTO);
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        mPhotoView.setOnClickListener((v1) -> {
            if (mPhotoView.getDrawable() != null) {
                ImageView image = new ImageView(getContext());
                image.setImageDrawable(mPhotoView.getDrawable());

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setView(image).create().show();
            }
        });

        return v;
    }

    void updateCrime() {
        CrimeLab.get(getContext()).updateCrime(mCrime);
        callBacks.onCrimeUpdated(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            mCrime.setmDate((Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE));
            updateCrime();
            UpdateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri uri = data.getData();

            // Определение полей, значения которых должны быть возвращены
            String[] queryField = new String[] {ContactsContract.Contacts.DISPLAY_NAME};

            // Теперь выполняется запрос в бд с контактами. На выходе получается объект класса Cursor
            Cursor c = getActivity().getContentResolver().query(uri, queryField,null,null,null);

            try {
                // Проверка результата
                if (c.getCount() == 0) return;

                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally {
                 c.close();
            }
        } else if (requestCode == REQUEST_PHOTO && data != null) {
            Uri uri = FileProvider.getUriForFile(getActivity(),"com.incorcadit16.criminalintent.fileprovider",mPhotoFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updateCrime();
            updatePhotoView();
        }
    }

    private void UpdateDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        mCrimeButton.setText(format.format(mCrime.getmDate()));
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_desription));
        } else {
            Bitmap bitmap = PicturesUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_desription));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callBacks = null;
    }

    public void returnResult() {
        getActivity().setResult(Activity.RESULT_OK);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.ismSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEEE, MMM dd, yyyy";
        String date = DateFormat.format(dateFormat,mCrime.getmDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null)
            suspect = getString(R.string.crime_report_no_suspect);
        else
            suspect = getString(R.string.crime_report_suspect,suspect);

        String report = getString(R.string.crime_report, mCrime.getmTitle(), date,solvedString,suspect);
        return report;
    }
}
