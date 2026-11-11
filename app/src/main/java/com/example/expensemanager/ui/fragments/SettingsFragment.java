package com.example.expensemanager.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.expensemanager.R;
import com.example.expensemanager.database.BudgetDAO;
import com.example.expensemanager.database.TransactionDAO;
import com.example.expensemanager.database.UserDAO;
import com.example.expensemanager.ui.CategoryManagerActivity;
import com.example.expensemanager.ui.LoginActivity;
import com.example.expensemanager.utils.AppPrefs;
import com.example.expensemanager.utils.CsvExporter;
import com.example.expensemanager.utils.CurrencyUtils;
import com.example.expensemanager.utils.DateUtils;
import com.example.expensemanager.utils.DialogUtils;
import com.example.expensemanager.utils.NotificationHelper;
import com.example.expensemanager.utils.SampleDataGenerator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.util.Calendar;

/**
 * Tab Cài đặt: thông tin tài khoản, đổi mật khẩu, đăng xuất, ngân sách, danh mục, nhắc nhở, xuất CSV.
 */
public class SettingsFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvBudgetValue, tvReminderTime;
    private MaterialSwitch switchReminder;
    private View rowReminderTime;
    private boolean ignoreSwitch = false;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableReminder(true);
                } else {
                    setSwitchSilently(false);
                    Toast.makeText(requireContext(),
                            "Cần cấp quyền thông báo để bật nhắc nhở", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tvUserName = v.findViewById(R.id.tv_user_name);
        tvUserEmail = v.findViewById(R.id.tv_user_email);
        tvBudgetValue = v.findViewById(R.id.tv_budget_value);
        tvReminderTime = v.findViewById(R.id.tv_reminder_time);
        switchReminder = v.findViewById(R.id.switch_reminder);
        rowReminderTime = v.findViewById(R.id.row_reminder_time);

        v.findViewById(R.id.row_change_password).setOnClickListener(x -> showChangePasswordDialog());
        v.findViewById(R.id.row_logout).setOnClickListener(x -> confirmLogout());

        v.findViewById(R.id.row_budget).setOnClickListener(x -> {
            Calendar now = Calendar.getInstance();
            DialogUtils.showBudgetDialog(requireContext(), DateUtils.monthKey(now),
                    DateUtils.monthTitle(now), this::refresh);
        });
        v.findViewById(R.id.row_categories).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), CategoryManagerActivity.class)));
        v.findViewById(R.id.row_reminder).setOnClickListener(x -> switchReminder.toggle());
        rowReminderTime.setOnClickListener(x -> showTimePicker());
        v.findViewById(R.id.row_export).setOnClickListener(x -> exportCsv());
        v.findViewById(R.id.row_sample).setOnClickListener(x -> confirmSampleData());
        v.findViewById(R.id.row_reset).setOnClickListener(x -> confirmReset());

        switchReminder.setOnCheckedChangeListener((button, checked) -> {
            if (ignoreSwitch) return;
            if (checked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && !NotificationHelper.canPostNotifications(requireContext())) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    enableReminder(true);
                }
            } else {
                enableReminder(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        tvUserName.setText(AppPrefs.getUserName(requireContext()));
        tvUserEmail.setText(AppPrefs.getUserEmail(requireContext()));

        double limit = new BudgetDAO(requireContext()).getLimit(DateUtils.monthKey(Calendar.getInstance()));
        tvBudgetValue.setText(limit > 0 ? CurrencyUtils.format(limit) : "Chưa thiết lập");

        boolean enabled = AppPrefs.isReminderEnabled(requireContext());
        setSwitchSilently(enabled);
        updateReminderTimeLabel();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        TextInputLayout tilOld = view.findViewById(R.id.til_old_password);
        TextInputLayout tilNew = view.findViewById(R.id.til_new_password);
        TextInputLayout tilConfirm = view.findViewById(R.id.til_confirm_password);
        TextInputEditText etOld = view.findViewById(R.id.et_old_password);
        TextInputEditText etNew = view.findViewById(R.id.et_new_password);
        TextInputEditText etConfirm = view.findViewById(R.id.et_confirm_password);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi mật khẩu")
                .setView(view)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String oldPass = etOld.getText() == null ? "" : etOld.getText().toString().trim();
            String newPass = etNew.getText() == null ? "" : etNew.getText().toString().trim();
            String confirm = etConfirm.getText() == null ? "" : etConfirm.getText().toString().trim();

            tilOld.setError(null);
            tilNew.setError(null);
            tilConfirm.setError(null);

            if (oldPass.isEmpty()) {
                tilOld.setError("Vui lòng nhập mật khẩu hiện tại");
                return;
            }
            if (newPass.length() < 6) {
                tilNew.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
                return;
            }
            if (!newPass.equals(confirm)) {
                tilConfirm.setError("Mật khẩu xác nhận không khớp");
                return;
            }

            UserDAO userDAO = new UserDAO(requireContext());
            boolean updated = userDAO.changePassword(AppPrefs.getUserId(requireContext()), oldPass, newPass);
            if (!updated) {
                tilOld.setError("Mật khẩu hiện tại không đúng");
                return;
            }
            Toast.makeText(requireContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đăng xuất tài khoản?")
                .setMessage("Bạn sẽ quay về màn hình đăng nhập. Dữ liệu thu chi của bạn vẫn được lưu an toàn.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    AppPrefs.clearUserSession(requireContext());
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    private void setSwitchSilently(boolean checked) {
        ignoreSwitch = true;
        switchReminder.setChecked(checked);
        ignoreSwitch = false;
        rowReminderTime.setAlpha(checked ? 1f : 0.45f);
        rowReminderTime.setEnabled(checked);
    }

    private void updateReminderTimeLabel() {
        tvReminderTime.setText(DateUtils.formatTime(AppPrefs.getReminderHour(requireContext()),
                AppPrefs.getReminderMinute(requireContext())));
    }

    private void enableReminder(boolean enable) {
        AppPrefs.setReminderEnabled(requireContext(), enable);
        if (enable) {
            NotificationHelper.scheduleDailyReminder(requireContext());
            Toast.makeText(requireContext(), "Sẽ nhắc bạn lúc " + tvReminderTime.getText() + " mỗi ngày",
                    Toast.LENGTH_SHORT).show();
        } else {
            NotificationHelper.cancelDailyReminder(requireContext());
        }
        setSwitchSilently(enable);
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(AppPrefs.getReminderHour(requireContext()))
                .setMinute(AppPrefs.getReminderMinute(requireContext()))
                .setTitleText("Giờ nhắc nhở")
                .build();
        picker.addOnPositiveButtonClickListener(x -> {
            AppPrefs.setReminderTime(requireContext(), picker.getHour(), picker.getMinute());
            updateReminderTimeLabel();
            if (AppPrefs.isReminderEnabled(requireContext())) {
                NotificationHelper.scheduleDailyReminder(requireContext());
            }
        });
        picker.show(getParentFragmentManager(), "time_picker");
    }

    private void exportCsv() {
        if (new TransactionDAO(requireContext()).getAll().isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có giao dịch nào để xuất file CSV",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File file = CsvExporter.export(requireContext());
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_SUBJECT, "Dữ liệu chi tiêu");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Chia sẻ file CSV"));
            Toast.makeText(requireContext(), "Đã lưu: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Xuất dữ liệu thất bại: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void confirmSampleData() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tạo dữ liệu mẫu?")
                .setMessage("Thêm các giao dịch ngẫu nhiên trong 90 ngày gần nhất để xem thử biểu đồ và thống kê.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (d, w) -> {
                    int count = SampleDataGenerator.generate(requireContext());
                    Toast.makeText(requireContext(), "Đã tạo " + count + " giao dịch mẫu",
                            Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .show();
    }

    private void confirmReset() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa dữ liệu của tài khoản?")
                .setMessage("Tất cả giao dịch và ngân sách của tài khoản này sẽ bị xóa vĩnh viễn.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa hết", (d, w) -> {
                    new TransactionDAO(requireContext()).deleteAll();
                    new BudgetDAO(requireContext()).deleteAll();
                    Toast.makeText(requireContext(), "Đã xóa toàn bộ dữ liệu", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .show();
    }
}
