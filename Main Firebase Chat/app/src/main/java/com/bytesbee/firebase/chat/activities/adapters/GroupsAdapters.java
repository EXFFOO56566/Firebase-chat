package com.bytesbee.firebase.chat.activities.adapters;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_AUDIO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_DOCUMENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_IMAGE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_LOCATION;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_RECORDING;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_VIDEO;

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
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;

import java.util.ArrayList;

public class GroupsAdapters extends RecyclerView.Adapter<GroupsAdapters.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Groups> mGroups;
    private final Screens screens;

    public GroupsAdapters(Context mContext, ArrayList<Groups> groupsList) {
        this.mContext = mContext;
        this.mGroups = groupsList;
        screens = new Screens(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_groups, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Groups groups = mGroups.get(i);

        viewHolder.txtGroupName.setText(groups.getGroupName());

        try {
            Utils.setGroupImage(mContext, groups.getGroupImg(), viewHolder.imageView);
        } catch (Exception ignored) {
        }

        try {
            viewHolder.txtLastMsg.setVisibility(View.VISIBLE);
            viewHolder.txtLastDate.setVisibility(View.VISIBLE);
            viewHolder.imgPhoto.setVisibility(View.GONE);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
        try {
            if (Utils.isEmpty(groups.getType())) {
                if (Utils.isEmpty(groups.getLastMsg())) {
                    viewHolder.txtLastMsg.setText(mContext.getString(R.string.msgTapToStartChat));
                } else {
                    viewHolder.txtLastMsg.setText(groups.getLastMsg());
                }
            } else {
                viewHolder.imgPhoto.setVisibility(View.VISIBLE);
                if (groups.getType().equalsIgnoreCase(TYPE_IMAGE)) {
                    setImageAndText(viewHolder, R.string.lblPhoto, R.drawable.ic_small_photo);
                } else if (groups.getType().equalsIgnoreCase(TYPE_RECORDING)) {
                    setImageAndText(viewHolder, R.string.lblVoiceRecording, R.drawable.ic_small_recording);
                } else if (groups.getType().equalsIgnoreCase(TYPE_AUDIO)) {
                    setImageAndText(viewHolder, R.string.lblAudio, R.drawable.ic_small_audio);
                } else if (groups.getType().equalsIgnoreCase(TYPE_VIDEO)) {
                    setImageAndText(viewHolder, R.string.lblVideo, R.drawable.ic_small_video);
                } else if (groups.getType().equalsIgnoreCase(TYPE_DOCUMENT)) {
                    setImageAndText(viewHolder, R.string.lblDocument, R.drawable.ic_small_document);
                } else if (groups.getType().equalsIgnoreCase(TYPE_CONTACT)) {
                    setImageAndText(viewHolder, R.string.lblContact, R.drawable.ic_small_contact);
                } else if (groups.getType().equalsIgnoreCase(TYPE_LOCATION)) {
                    setImageAndText(viewHolder, R.string.lblLocation, R.drawable.ic_small_location);
                } else {
                    viewHolder.imgPhoto.setVisibility(View.GONE);
                    if (Utils.isEmpty(groups.getLastMsg())) {
                        viewHolder.txtLastMsg.setText(mContext.getString(R.string.msgTapToStartChat));
                    } else {
                        viewHolder.txtLastMsg.setText(groups.getLastMsg());

                    }
                }
            }
            if (Utils.isEmpty(groups.getLastMsgTime())) {
                viewHolder.txtLastDate.setText("");
            } else {
                viewHolder.txtLastDate.setText(Utils.formatDateTime(mContext, groups.getLastMsgTime()));
            }
        } catch (Exception ignored) {

        }

        viewHolder.imageView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                //screens.openGroupParticipantActivity(groups);
                screens.openProfilePictureActivity(groups);
            }
        });

        viewHolder.itemView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openGroupMessageActivity(groups);
            }
        });

    }

    private void setImageAndText(ViewHolder viewHolder, int msg, int photo) {
        viewHolder.txtLastMsg.setText(mContext.getString(msg));
        viewHolder.imgPhoto.setImageResource(photo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageView;
        public final TextView txtGroupName;
        private final TextView txtLastMsg;
        private final TextView txtLastDate;
        private final ImageView imgPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg);
            txtLastDate = itemView.findViewById(R.id.txtLastDate);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}
