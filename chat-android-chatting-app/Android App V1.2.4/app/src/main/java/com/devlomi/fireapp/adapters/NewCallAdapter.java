package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class NewCallAdapter extends RealmRecyclerViewAdapter<User, NewCallAdapter.UserHolder> {

    private List<User> userList;
    private Context context;
    private OnClickListener onUserClick;

    public NewCallAdapter(@Nullable OrderedRealmCollection data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        userList = data;
        this.context = context;
    }


    public void setOnUserClick(OnClickListener onUserClick) {
        this.onUserClick = onUserClick;
    }


    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_new_call, parent, false);
        return new UserHolder(row);
    }


    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView tvUsername;
        private ImageButton btnCall;
        private ImageButton btnVideoCall;


        public UserHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnVideoCall = itemView.findViewById(R.id.btn_video_call);

        }

        public void bind(final User user) {


            tvUsername.setText(user.getUserName());


            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onUserClick != null)
                        onUserClick.onUserClick(view, user,false);
                }
            });

            btnVideoCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onUserClick != null)
                        onUserClick.onUserClick(view, user,true);
                }
            });

            Glide.with(context).load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).asBitmap().into(profileImage);


        }


    }

    public interface OnClickListener {
        void onUserClick(View view, User user, boolean isVideo);
    }
}
