package com.bytesbee.firebase.chat.activities.adapters;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.ONE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.constants.IGroupListener;
import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.bytesbee.firebase.chat.activities.views.smoothcb.SmoothCheckBox;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupsUserAdapters extends RecyclerView.Adapter<GroupsUserAdapters.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final Context mContext;
    private final ArrayList<User> mUsers;
    private final ArrayList<User> mSelectedUsers;
    private final List<String> mSelectedMembersId;
    private final IGroupListener groupListener;
    private final boolean isEditGroup;
    private final Groups groups;
    private final Set<String> mDeletedMembersId;

    public GroupsUserAdapters(Context mContext, ArrayList<User> usersList, ArrayList<User> mSelectedUsers, List<String> mSelectedMembersId, final Set<String> mDeletedMembersId, final boolean isEditGroup, final Groups groups, IGroupListener groupListener) {
        this.mContext = mContext;
        this.mUsers = Utils.removeDuplicates(usersList);
        this.mSelectedUsers = mSelectedUsers;
        this.mSelectedMembersId = mSelectedMembersId;
        this.mDeletedMembersId = mDeletedMembersId;
        this.groupListener = groupListener;

        this.isEditGroup = isEditGroup;
        this.groups = groups;

        if (isEditGroup) {
            for (int i = 0; i < mUsers.size(); i++) {
                if (groups.getMembers().contains(mUsers.get(i).getId())) {
                    this.mSelectedUsers.add(mUsers.get(i));
                    this.mSelectedMembersId.add(mUsers.get(i).getId());
                }
            }
            groupListener.setSubTitle();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_users_group, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        final User user = mUsers.get(position);
        final String strAbout = user.getAbout();

        viewHolder.txtUsername.setText(user.getUsername());
        Utils.setProfileImage(mContext, user.getImageURL(), viewHolder.imageView);

        viewHolder.txtLastMsg.setVisibility(View.VISIBLE);

        if (Utils.isEmpty(strAbout)) {
            viewHolder.txtLastMsg.setText(mContext.getString(R.string.strAboutStatus));
        } else {
            viewHolder.txtLastMsg.setText(strAbout);
        }

        if (user.getIsOnline() == STATUS_ONLINE) {
            viewHolder.imgOn.setVisibility(View.VISIBLE);
            viewHolder.imgOff.setVisibility(View.GONE);
        } else {
            viewHolder.imgOn.setVisibility(View.GONE);
            viewHolder.imgOff.setVisibility(View.VISIBLE);
        }

        viewHolder.cb.setOnCheckedChangeListener((checkBox, isChecked) -> user.setChecked(isChecked));

        if (isEditGroup) {
            viewHolder.cb.setChecked(groups.getMembers().contains(user.getId()));
        } else {
            viewHolder.cb.setChecked(user.isChecked());
        }

        viewHolder.imageView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                final Screens screens = new Screens(mContext);
                screens.openViewProfileActivity(user.getId());
            }
        });

        viewHolder.itemView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                user.setChecked(!user.isChecked());
                viewHolder.cb.setChecked(user.isChecked(), true);
                if (user.isChecked()) {
                    mSelectedUsers.add(user);
                    mSelectedMembersId.add(user.getId());
                    mDeletedMembersId.remove(user.getId());
                } else {
                    mSelectedUsers.remove(user);
                    mSelectedMembersId.remove(user.getId());
                    mDeletedMembersId.add(user.getId());
                }
                groupListener.setSubTitle();
            }
        });

        viewHolder.cb.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                Utils.sout("Click on cb");
                user.setChecked(!user.isChecked());
                viewHolder.cb.setChecked(user.isChecked(), true);
                if (user.isChecked()) {
                    mSelectedUsers.add(user);
                    mSelectedMembersId.add(user.getId());
                    mDeletedMembersId.remove(user.getId());
                } else {
                    mSelectedUsers.remove(user);
                    mSelectedMembersId.remove(user.getId());
                    mDeletedMembersId.add(user.getId());
                }
                groupListener.setSubTitle();
            }
        });

    }

    @NonNull
    @NotNull
    @Override
    public String getSectionName(int position) {
        if (!Utils.isEmpty(mUsers)) {
            return mUsers.get(position).getUsername().substring(ZERO, ONE);
        } else {
            return null;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageView;
        public final TextView txtUsername;
        private final ImageView imgOn;
        private final ImageView imgOff;
        private final TextView txtLastMsg;
        private final SmoothCheckBox cb;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            imgOn = itemView.findViewById(R.id.imgOn);
            imgOff = itemView.findViewById(R.id.imgOff);
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg);
            cb = itemView.findViewById(R.id.scb);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
