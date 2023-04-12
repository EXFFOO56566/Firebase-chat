package com.devlomi.fireapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.PhoneNumber;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

/**
 * Created by Devlomi on 11/01/2018.
 */

public class NumbersForContactAdapter extends CheckableChildRecyclerViewAdapter<NumbersForContactAdapter.ContactNameHolder, NumbersForContactAdapter.PhoneNumberHolder> {

    public NumbersForContactAdapter(List<? extends CheckedExpandableGroup> groups) {
        super(groups);
    }


    @Override
    public ContactNameHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact_picker, parent, false);
        return new ContactNameHolder(row);
    }


    @Override
    public PhoneNumberHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_phone_number, parent, false);
        return new PhoneNumberHolder(row);

    }

    @Override
    public void onBindCheckChildViewHolder(final PhoneNumberHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final PhoneNumber phoneNumber = (PhoneNumber) group.getItems().get(childIndex);
        holder.setPhoneNumber(phoneNumber.getNumber());
    }

    @Override
    public void onBindGroupViewHolder(ContactNameHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setContactName(group);
    }

    class ContactNameHolder extends GroupViewHolder {
        private TextView tvContactNamePick;

        public ContactNameHolder(View itemView) {
            super(itemView);
            tvContactNamePick = itemView.findViewById(R.id.tv_contact_name_pick);
        }

        public void setContactName(ExpandableGroup group) {
            tvContactNamePick.setText(group.getTitle());
        }
    }

    class PhoneNumberHolder extends CheckableChildViewHolder {
        private CheckedTextView checkTextViewNumber;


        public PhoneNumberHolder(View itemView) {
            super(itemView);
            checkTextViewNumber = itemView.findViewById(R.id.check_text_view_number);
        }

        @Override
        public Checkable getCheckable() {
            return checkTextViewNumber;
        }

        public void setPhoneNumber(String number) {
            checkTextViewNumber.setText(number);
        }
    }

    //set all items as checked
    public void toggleAllGroups() {
        for (int i = 0; i < expandableList.expandedGroupIndexes.length; i++) {
            toggleGroup(getFlattenedGroupPosition(i));
        }
    }

    private int getFlattenedGroupPosition(int groupIndex) {
        int runningTotal = 0;
        for (int i = 0; i < groupIndex; i++) {
            runningTotal += numberOfVisibleItemsInGroup(i);
        }
        return runningTotal;
    }

    private int numberOfVisibleItemsInGroup(int group) {
        if (expandableList.expandedGroupIndexes[group]) {
            return expandableList.groups.get(group).getItemCount() + 1;
        } else {
            return 1;
        }
    }


}
