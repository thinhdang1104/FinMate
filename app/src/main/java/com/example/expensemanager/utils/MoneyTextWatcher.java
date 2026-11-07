package com.example.expensemanager.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Tự động thêm dấu chấm phân cách hàng nghìn khi người dùng nhập số tiền.
 */
public class MoneyTextWatcher implements TextWatcher {

    private static final int MAX_DIGITS = 13; // tối đa 9.999.999.999.999 ₫

    private final EditText editText;
    private boolean editing = false;

    public MoneyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (editing) return;
        editing = true;
        String digits = s.toString().replaceAll("[^0-9]", "");
        if (digits.length() > MAX_DIGITS) digits = digits.substring(0, MAX_DIGITS);
        // Bỏ số 0 ở đầu
        while (digits.length() > 1 && digits.startsWith("0")) digits = digits.substring(1);
        String formatted = digits.isEmpty() ? "" : CurrencyUtils.formatNumber(Long.parseLong(digits));
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editing = false;
    }
}
