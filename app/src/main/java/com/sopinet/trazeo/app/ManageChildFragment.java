package com.sopinet.trazeo.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.sopinet.android.nethelper.NetHelper;

import org.androidannotations.annotations.EFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

@EFragment
public class ManageChildFragment extends Fragment{

    ChildrenActivity context;

    EditText childName;
    EditText school;
    EditText date;
    Spinner genderSpinner;
    Button btnConfirm;

    String date_birth;

    public ManageChildFragment() {}

    public static ManageChildFragment newInstance(String childId, String childName, String scholl, String date_birth, String gender) {
        ManageChildFragment manageChildFragment = new ManageChildFragment();
        Bundle args = new Bundle();
        args.putString("childId", childId);
        args.putString("childName", childName);
        args.putString("scholl", scholl);
        args.putString("date_birth", date_birth);
        args.putString("gender", gender);
        manageChildFragment.setArguments(args);
        return manageChildFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String date = getArguments().getString("date_birth");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");
        Date d;
        try {
             d = sdf.parse(date);
             date_birth = output.format(d);
        } catch (ParseException e) {
            Log.d("WARNING", "date_birth está vacío o nulo.");
            date_birth = "";
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = (ChildrenActivity) getActivity();

        if (context.myPrefs.new_user().get() != 2) {
            this.btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendAddChildren(true);
                }
            });
        } else {
            this.btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendAddChildren(false);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_child, container, false);

        this.childName = (EditText) root.findViewById(R.id.childName);
        this.school = (EditText) root.findViewById(R.id.school);
        this.date = (EditText) root.findViewById(R.id.date);
        this.genderSpinner = (Spinner) root.findViewById(R.id.genderSpinner);
        this.btnConfirm = (Button) root.findViewById(R.id.btnConfirm);

        this.childName.setText(getArguments().getString("childName"));
        this.school.setText(getArguments().getString("scholl"));
        this.date.setText(date_birth);

        if (getArguments().getString("gender").equals("girl")) {
            genderSpinner.setSelection(1);
        } else {
            genderSpinner.setSelection(0);
        }

        this.date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    showDatePickerDialog();
            }
        });

        this.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        return root;
    }

    private void sendAddChildren(boolean goBack) {

        if (childName.getText().toString().trim().equals("")) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_title))
                    .setContentText(getString(R.string.add_name))
                    .setConfirmText(getString(R.string.accept_button))
                    .show();
        } else if(NetHelper.isOnline(context)) {
            String gender;
            if (genderSpinner.getSelectedItemPosition() == 0)
                gender = "boy";
            else
                gender = "girl";

            String childId = getArguments().getString("childId");

            context.manageChilds(childId, childName.getText().toString(), school.getText().toString(),
                    date.getText().toString(), gender, goBack);
        } else {
            context.showError();
        }
    }

    public void showDatePickerDialog() {
        int mYear, mMonth, mDay;

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        date.setText(dayOfMonth + "-"
                                + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

}
