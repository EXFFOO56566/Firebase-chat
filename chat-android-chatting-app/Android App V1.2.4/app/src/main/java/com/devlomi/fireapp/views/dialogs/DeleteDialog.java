package com.devlomi.fireapp.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;

import com.devlomi.fireapp.R;


//this will show a Confirmation delete dialog
//with a check box if needed to delete the file from storage
public class DeleteDialog extends AlertDialog.Builder {
    //onClickListener callback
    private OnFragmentInteractionListener mListener;
    private OnItemClick onItemClick;
    private Context context;
    private AppCompatCheckBox chbDeleteFromPhone;
    private boolean isContainsMedia;
    private boolean showItems = false;
    private String mTitle = null;

    public DeleteDialog(Context context, boolean isContainsMedia) {
        super(context);
        this.context = context;
        this.isContainsMedia = isContainsMedia;
    }

    public DeleteDialog(Context context, boolean isContainsMedia, boolean showItems) {
        super(context);
        this.context = context;
        this.isContainsMedia = isContainsMedia;
        this.showItems = showItems;
    }

    public void setMTitle(String title) {
        mTitle = title;
    }


    @Override
    public AlertDialog show() {
        //if this is true then show the checkbox (Delete from phone)
        if (isContainsMedia) {
            View view = LayoutInflater.from(context).inflate(R.layout.fragment_delete_dialog, null);
            chbDeleteFromPhone = view.findViewById(R.id.chb_delete_from_phone);
            setView(view);
            chbDeleteFromPhone.setVisibility(View.VISIBLE);
        }

        Resources resources = context.getResources();

        if (mTitle == null)
            setTitle(R.string.delete_messages_confirmation);
        else
            setTitle(mTitle);


        if (showItems) {
            setItems(new CharSequence[]
                            {resources.getString(R.string.delete_for_me), resources.getString(R.string.cancel).toUpperCase(), resources.getString(R.string.delete_for_everyone)},
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            if (onItemClick != null) {
                                if (chbDeleteFromPhone != null) {
                                    onItemClick.onClick(which, chbDeleteFromPhone.isChecked());
                                } else {
                                    onItemClick.onClick(which, false);
                                }
                            }
                        }
                    });
        } else {

            setPositiveButton(R.string.delete_for_me, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //pass callback to the activity
                    if (isContainsMedia)
                        mListener.onPositiveClick(chbDeleteFromPhone.isChecked());
                    else
                        mListener.onPositiveClick(false);
                }
            });
            setNegativeButton(resources.getString(R.string.cancel).toUpperCase(), null);
        }

        //show the dialog
        return super.show();
    }

    public void setmListener(OnFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnFragmentInteractionListener {
        void onPositiveClick(boolean isDeleteChecked);
    }

    public interface OnItemClick {
        void onClick(int pos, boolean isDeleteChecked);
    }


}
