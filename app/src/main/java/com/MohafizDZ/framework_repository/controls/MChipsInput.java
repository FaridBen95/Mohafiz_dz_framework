package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.util.AttributeSet;

import com.MohafizDZ.own_distributor.R;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.views.ChipsInputEditText;

public class MChipsInput extends ChipsInput{
    private ChipsInputEditText editText;

    public MChipsInput(Context context) {
        super(context);
    }

    public MChipsInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void onTextChanged(CharSequence text) {}

    @Override
    public ChipsInputEditText getEditText() {
        ChipsInputEditText editText = new ChipsInputEditText(getContext());
        editText.setHintTextColor(getContext().getResources().getColor(R.color.android_grey_light));
        editText.clearFocus();
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        return editText;
    }
}
