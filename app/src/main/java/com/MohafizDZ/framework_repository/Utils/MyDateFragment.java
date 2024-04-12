package com.MohafizDZ.framework_repository.Utils;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


import com.MohafizDZ.empty_project.R;

import java.util.Calendar;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyDateFragment extends DialogFragment {

    IMyDatePicker datePickerListener;

    public MyDateFragment(IMyDatePicker datePickerListener){
        this.datePickerListener = datePickerListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), com.facebook.R.style.Base_Theme_AppCompat_Dialog,
                datePickerListener,
                yy, mm, dd);
        Long minDate = datePickerListener.setMinDate();
        Long maxDate = datePickerListener.setMaxDate();
        if(minDate != null){
            datePickerDialog.getDatePicker().setMinDate(minDate);
        }
        if(maxDate != null){
            datePickerDialog.getDatePicker().setMaxDate(maxDate);
        }
        return datePickerDialog;
    }


}
