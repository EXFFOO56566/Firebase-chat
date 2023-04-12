package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.User;

import java.util.List;

public class PartialGroupUsersAdapter extends RecyclerView.Adapter {
    private List<User> userList;
    private Context context;

    public PartialGroupUsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_parital_group_user, parent, false);
        return new PartialUsersHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        User user = userList.get(position);
        PartialUsersHolder holder = (PartialUsersHolder) viewHolder;
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class PartialUsersHolder extends RecyclerView.ViewHolder {
        private ImageView imgUser;
        private TextView tvUsername;


        public PartialUsersHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.img_user);
            tvUsername = itemView.findViewById(R.id.tv_username);
        }

        public void bind(User user) {
            Glide.with(context).load(user.getPhoto())
                    .placeholder(AppCompatResources.getDrawable(context, R.drawable.user_img_wrapped))
                    .into(imgUser);
            tvUsername.setText(user.getUserName());
        }
    }
}
