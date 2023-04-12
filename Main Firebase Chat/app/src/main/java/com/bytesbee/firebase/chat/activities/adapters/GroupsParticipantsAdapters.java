package com.bytesbee.firebase.chat.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class GroupsParticipantsAdapters extends RecyclerView.Adapter<GroupsParticipantsAdapters.ViewHolder> {

    private final Context mContext;
    private final ArrayList<User> mUsers;
    private final Screens screens;

    public GroupsParticipantsAdapters(Context mContext, ArrayList<User> usersList) {
        this.mContext = mContext;
        this.mUsers = Utils.removeDuplicates(usersList);
        this.screens = new Screens(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_group_participants, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        final String strAbout = user.getAbout();

        viewHolder.txtUsername.setText(user.getUsername());
        if (user.getUsername().equalsIgnoreCase(mContext.getString(R.string.strYou))) {
            Utils.setProfileImage(mContext, user.getMyImg(), viewHolder.imageView);
        } else {
            Utils.setProfileImage(mContext, user.getImageURL(), viewHolder.imageView);
        }

        viewHolder.txtLastMsg.setVisibility(View.VISIBLE);
        if (Utils.isEmpty(strAbout)) {
            viewHolder.txtLastMsg.setText(mContext.getString(R.string.strAboutStatus));
        } else {
            viewHolder.txtLastMsg.setText(strAbout);
        }

        viewHolder.txtAdmin.setVisibility(View.GONE);

        if (user.isAdmin()) {
            viewHolder.txtAdmin.setVisibility(View.VISIBLE);
        }

        viewHolder.imageView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openViewProfileActivity(user.getId());
            }
        });

        viewHolder.itemView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(user.getId())) {
                    screens.openUserMessageActivity(user.getId());
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageView;
        public final TextView txtUsername;
        private final TextView txtLastMsg;
        private final TextView txtAdmin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg);
            txtAdmin = itemView.findViewById(R.id.txtAdmin);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
