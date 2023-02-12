package com.MohafizDZ.framework_repository.Utils;

import android.app.DatePickerDialog;


public interface IMyDatePicker extends DatePickerDialog.OnDateSetListener {
    Long setMinDate();
    Long setMaxDate();
}
