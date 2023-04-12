package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.ForwardActivity;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.AppVerUtil;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.hidely.hidelyviews.HidelyImageView;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

//the RealmRecyclerViewAdapter provides autoUpdate feature
//which will handle changes in list automatically with smooth animations
public class ForwardAdapter extends RealmRecyclerViewAdapter<User, RecyclerView.ViewHolder> {
    private Context context;
    private List<User> list;
    ForwardActivity activity;
    List<User> selectedForwardedUsers;
    OnUserClick onUserClick;
    List<User> currentGroupUsers;
    private boolean isAddingUsersToGroup = false;
    private boolean isBroadcast = false;

    public ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.onUserClick = onUserClick;
        activity = (ForwardActivity) context;
    }


    public ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, List<User> currentGroupUsers, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.onUserClick = onUserClick;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.currentGroupUsers = currentGroupUsers;
        isAddingUsersToGroup = currentGroupUsers != null;
        activity = (ForwardActivity) context;
    }

    public ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, List<User> currentGroupUsers, boolean isBroadcast, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.onUserClick = onUserClick;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.currentGroupUsers = currentGroupUsers;
        this.isBroadcast = isBroadcast;
        isAddingUsersToGroup = currentGroupUsers != null;
        activity = (ForwardActivity) context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forward, parent, false);
        return new ForwardHolder(row);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final ForwardHolder mHolder = (ForwardHolder) holder;
        final User user = list.get(position);
        //get the name from phonebook
        String name = user.getUserName();
        mHolder.tvTitle.setText(name);


        if (currentGroupUsers != null) {
            if (currentGroupUsers.contains(user)) {
                mHolder.tvDesc.setText(isBroadcast ? R.string.user_already_added_to_broadcast : R.string.user_already_added_to_group);
                mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.BOLD_ITALIC);
                mHolder.tvDesc.setTextColor(context.getResources().getColor(R.color.colorsecondary_text));
            } else {
                if (!user.isGroupBool() && !AppVerUtil.isAppSupportsGroups(user.getAppVer())) {
                    mHolder.tvDesc.setText(R.string.this_user_has_old_version);
                    mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.BOLD_ITALIC);
                    mHolder.tvDesc.setTextColor(context.getResources().getColor(R.color.colorsecondary_text));
                } else
                    //get user status
                    setUserStatus(mHolder, user);

            }


        } else {
            //get user status
            setUserStatus(mHolder, user);

        }
        //on select user
        mHolder.rlltBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if user is blocked
                if (user.isBlocked())
                    Snackbar.make(activity.findViewById(android.R.id.content), R.string.user_is_blocked, Snackbar.LENGTH_SHORT).show();

                    //otherwise select this user
                else
                    itemSelected(user, mHolder);
            }
        });

        //keep groupUsers selected when scrolling
        if (selectedForwardedUsers.contains(user))
            mHolder.selectedCircle.setVisibility(View.VISIBLE);
        else
            mHolder.selectedCircle.setVisibility(View.INVISIBLE);


        loadUserPhoto(user, mHolder.userProfile);


    }

    private void setUserStatus(ForwardHolder mHolder, User user) {
        mHolder.tvDesc.setText(user.getStatus() == null ? "" : user.getStatus());
        mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.NORMAL);
        mHolder.tvDesc.setTextColor(context.getResources().getColor(R.color.colorTextDesc));
    }

    private void itemSelected(User user, ForwardHolder mHolder) {
        if (isAddingUsersToGroup && currentGroupUsers.contains(user) || isAddingUsersToGroup && !AppVerUtil.isAppSupportsGroups(user.getAppVer())) {
        }

        //if user is selected un-select
        else if (selectedForwardedUsers.contains(user)) {
            itemRemoved(user);

            //if there are no groupUsers selected hide the snackbar
            if (selectedForwardedUsers.isEmpty())
                activity.hideSnackbar();

            //hide select icon
            mHolder.selectedCircle.hide();


            //otherwise show snackbar and the select icon
            //and add the user to the list
        } else {
            activity.showSnackbar();
            mHolder.selectedCircle.show();
            itemAdded(user);
        }
    }

    private void itemRemoved(User user) {
        selectedForwardedUsers.remove(user);
        activity.updateSelectedUsers();
        if (onUserClick != null)
            onUserClick.onChange(user, false);
    }

    private void itemAdded(User user) {
        selectedForwardedUsers.add(user);
        activity.updateSelectedUsers();
        if (onUserClick != null)
            onUserClick.onChange(user, true);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ForwardHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlltBody;
        private ImageView userProfile;
        TextView tvTitle, tvDesc;
        HidelyImageView selectedCircle;


        public ForwardHolder(View itemView) {
            super(itemView);
            rlltBody = (RelativeLayout) itemView.findViewById(R.id.container_layout);
            userProfile = (ImageView) itemView.findViewById(R.id.user_photo);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_name);
            tvDesc = (TextView) itemView.findViewById(R.id.tv_status);
            selectedCircle = itemView.findViewById(R.id.img_selected);
        }
    }


    private void loadUserPhoto(final User user, final ImageView imageView) {
        if (user == null) return;
        if (user.getUid() == null) return;


        if (user.isBroadcastBool()) {
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_broadcast_with_bg));
        } else if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(context).load(bytes).asBitmap().into(imageView);
        }

        //check if there is a new photo for this user
        if (!isBroadcast) {
            FireManager.checkAndDownloadUserPhoto(user, new FireManager.OnGetUserThumbImg() {
                @Override
                public void onGetThumb(String thumbImg) {
                    try {
                        Glide.with(context).load(BitmapUtils.encodeImageAsBytes(thumbImg)).asBitmap().into(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
        }

    }

    public List<User> getSelectedForwardedUsers() {
        return selectedForwardedUsers;
    }

    public interface OnUserClick {
        void onChange(User user, boolean added);
    }


}
