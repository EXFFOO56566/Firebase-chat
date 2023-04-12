package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.FireManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
//this will show the users in a group or broadcast inside 'UserDetailsActivity'
public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantHolder> {
    private Context context;
    private List<String> adminUids;
    private List<User> userList;
    private OnParticipantClick onParticipantClick;

    public ParticipantsAdapter(Context context, List<String> adminUids, List<User> userList, OnParticipantClick onParticipantClick) {
        this.context = context;
        this.adminUids = adminUids;
        this.userList = userList;
        this.onParticipantClick = onParticipantClick;
    }

    public ParticipantsAdapter(Context context, List<User> userList, OnParticipantClick onParticipantClick) {
        this.context = context;
        this.adminUids = new ArrayList<>();//just create empty list
        this.userList = userList;
        this.onParticipantClick = onParticipantClick;
    }

    @NonNull
    @Override
    public ParticipantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_group_participant, parent, false);
        return new ParticipantHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantHolder holder, int position) {
        final User user = userList.get(position);
        holder.bind(user);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!user.getUid().equals(FireManager.getUid()) && onParticipantClick != null)
                    onParticipantClick.onClick(user, view);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!user.getUid().equals(FireManager.getUid()) && onParticipantClick != null)
                    onParticipantClick.onLongClick(user, view);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ParticipantHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgViewGroupParticipant;
        private TextView tvUsernameGroup;
        private TextView tvUserStatus;
        private TextView tvGroupAdminTag;


        public ParticipantHolder(View itemView) {
            super(itemView);
            imgViewGroupParticipant = itemView.findViewById(R.id.img_view_group_participant);
            tvUsernameGroup = itemView.findViewById(R.id.tv_username_group);
            tvUserStatus = itemView.findViewById(R.id.tv_user_status);
            tvGroupAdminTag = itemView.findViewById(R.id.tv_group_admin_tag);
        }

        public void bind(User user) {
            Glide.with(context).load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).asBitmap().into(imgViewGroupParticipant);

            if (user.getUid().equals(FireManager.getUid())) {
                tvUsernameGroup.setText(context.getResources().getString(R.string.you));
            } else
                tvUsernameGroup.setText(user.getUserName());

            tvUserStatus.setText(user.getStatus());

            if (adminUids.contains(user.getUid())) {
                tvGroupAdminTag.setVisibility(View.VISIBLE);
            } else {
                tvGroupAdminTag.setVisibility(View.GONE);
            }
        }
    }

    public interface OnParticipantClick {
        void onClick(User user, View view);

        void onLongClick(User user, View view);
    }
}
