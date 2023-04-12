package com.devlomi.fireapp.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.devlomi.fireapp.R;

public class IgnoreBatteryDialog extends AlertDialog.Builder {
    Context context;
    private OnDialogClickListener onDialogClickListener;

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }

    public IgnoreBatteryDialog(Context context) {
        super(context);
        this.context = context;
    }

    public IgnoreBatteryDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    public AlertDialog show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ignore_battery, null);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        AppCompatCheckBox checkBox = view.findViewById(R.id.chb_dont_show);
        setView(view);

        String message = String.format(context.getString(R.string.ignore_battery_dialog_message, context.getString(R.string.app_name)));
        tvMessage.setText(message);
        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onDialogClickListener != null)
                    onDialogClickListener.onCancelClick(checkBox.isChecked());
            }
        });

        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onDialogClickListener != null)
                    onDialogClickListener.onOk();
            }
        });
        return super.show();
    }

    public interface OnDialogClickListener {
        void onCancelClick(boolean checkBoxChecked);

        void onOk();
    }
}
