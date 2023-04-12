package com.devlomi.fireapp.activities.settings;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.utils.StringUtils;

/**
 * Created by Devlomi on 25/03/2018.
 */

/**
 * this class is to make Custom Multi Select List.
 * it is used in Media Auto Download Settings
 */

public class SettingsMultiSelectPreference extends MultiSelectListPreference {

    Context context;


    public SettingsMultiSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initContext(context);
        setSummary();
    }

    public SettingsMultiSelectPreference(Context context) {
        super(context);
        initContext(context);
        setSummary();
    }

    private void initContext(Context context) {
        this.context = context;
    }

    //setting summary values depending on what settings provided
    private void setSummary() {

        String summaryText = "";
        String separator = " , ";
        if (getValues().isEmpty()) {
            setSummary(R.string.no_media_summary);

        } else {

            for (String s : getValues()) {
                if (s.equals("0"))
                    summaryText += context.getString(R.string.photos) + separator;

                if (s.equals("1"))
                    summaryText += context.getString(R.string.audio) + separator;
                if (s.equals("2"))
                    summaryText += context.getString(R.string.videos) + separator;
                if (s.equals("3"))
                    summaryText += context.getString(R.string.files) + separator;

            }


            //removing separator from last word
            summaryText = StringUtils.removeExtraSeparators(summaryText, separator);


            setSummary(summaryText);
        }

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult)
            setSummary();

    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object rawDefaultValue) {
        super.onSetInitialValue(restoreValue, rawDefaultValue);
        setSummary();


    }
}
