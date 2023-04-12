package com.devlomi.fireapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.PhoneNumber;

import io.realm.RealmList;

/**
 * Created by Devlomi on 18/01/2018.
 */

public class ContactDetailsAdapter extends RecyclerView.Adapter {

    private RealmList<PhoneNumber> contactList;

    public ContactDetailsAdapter(RealmList<PhoneNumber> contactList) {
        this.contactList = contactList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contact_details, parent, false);
        return new ContactNumberHolder(row);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ContactNumberHolder mHolder = (ContactNumberHolder) holder;
        PhoneNumber phoneNumber = contactList.get(position);
        mHolder.tvNumber.setText(phoneNumber.getNumber());

        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onItemClick(v,holder.getAdapterPosition());
                }
            }
        });

        mHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemClick != null)
                    onItemClick.onItemLongClick(v,holder.getAdapterPosition());
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    class ContactNumberHolder extends RecyclerView.ViewHolder {
        private TextView tvNumber;

        public ContactNumberHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_number_details);
        }
    }

    public interface OnItemClick {
        void onItemClick(View view,int pos);

        void onItemLongClick(View view,int pos);
    }

    OnItemClick onItemClick;

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }
}
