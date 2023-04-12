package com.devlomi.fireapp.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.PhoneNumber;

import io.realm.RealmList;

//this will show a Choose number dialog if the contact has more than one number
public class DialogChoseNumber extends AlertDialog.Builder {
    AlertDialog alertDialog;
    //get the numbers
    RealmList<PhoneNumber> numbers;
    OnItemClickListener onItemClickListener;
    Context context;

    public DialogChoseNumber(Context context, RealmList<PhoneNumber> numbers) {
        super(context);
        this.context = context;
        this.numbers = numbers;
    }

    @Override
    public AlertDialog show() {


        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //add contacts vertically
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (PhoneNumber number : numbers) {
            //create textView for every number
            final TextView textView = new TextView(context);
            //set number
            textView.setText(number.getNumber());
            //add some padding
            textView.setPadding(30, 30, 30, 30);
            //add textView to linear layout
            linearLayout.addView(textView);

            //pass onClick listener to activity
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onClick(textView.getText().toString());

                    //dismiss dialog after dismiss
                    alertDialog.dismiss();

                }
            });
        }
//set dialog title
        setTitle(R.string.choose_number);
        //set the custom layout that we've created to the dialog
        setView(linearLayout);
        //show dialog
        alertDialog = super.show();
        return alertDialog;
    }


    public interface OnItemClickListener {
        void onClick(String number);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
