package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.FireCallType;
import com.devlomi.fireapp.model.realms.FireCall;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.TimeHelper;
import com.devlomi.hidely.hidelyviews.HidelyImageView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class CallsAdapter extends RealmRecyclerViewAdapter<FireCall, CallsAdapter.PhoneCallHolder> {

    private List<FireCall> fireCallList;
    private List<FireCall> originalList;
    private List<FireCall> selectedItemForActionMode;
    private Context context;
    private OnClickListener onPhoneCallClick;

    public CallsAdapter(@Nullable OrderedRealmCollection<FireCall> data,
                        List<FireCall> selectedItemForActionMode, Context context, OnClickListener onPhoneCallClick) {
        super(data, true);
        this.fireCallList = data;
        originalList = fireCallList;
        this.selectedItemForActionMode = selectedItemForActionMode;
        this.context = context;
        this.onPhoneCallClick = onPhoneCallClick;
    }



    @NonNull
    @Override
    public PhoneCallHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_call, parent, false);
        return new PhoneCallHolder(row);
    }


    @Override
    public void onBindViewHolder(@NonNull PhoneCallHolder holder, int position) {
        holder.bind(fireCallList.get(position));
    }

    @Override
    public int getItemCount() {
        return fireCallList.size();
    }

    class PhoneCallHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView tvUsername;
        private TextView tvCallTime;
        private ImageView callType;
        private ImageButton btnCall;
        private HidelyImageView imgSelected;


        public PhoneCallHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvCallTime = itemView.findViewById(R.id.tv_call_time);
            callType = itemView.findViewById(R.id.call_type);
            btnCall = itemView.findViewById(R.id.btn_call);
            imgSelected = itemView.findViewById(R.id.img_selected);
        }

        public void bind(final FireCall fireCall) {
            User user = fireCall.getUser();

            if (user != null) {
                tvUsername.setText(user.getUserName());
                Glide.with(context).load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).asBitmap().into(profileImage);
            } else
                tvUsername.setText(fireCall.getPhoneNumber());

            callType.setImageDrawable(getPhoneCallType(fireCall.getType()));
            btnCall.setImageResource(fireCall.isVideo() ? R.drawable.ic_videocam_blue : R.drawable.ic_phone_blue);

            tvCallTime.setText(TimeHelper.getCallTime(fireCall.getTimestamp()));

            if (selectedItemForActionMode.contains(fireCall)) {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
                imgSelected.setVisibility(View.VISIBLE);

            } else {
                itemView.setBackgroundColor(-1);
                imgSelected.setVisibility(View.INVISIBLE);
            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhoneCallClick != null)
                        onPhoneCallClick.onItemClick(imgSelected, view, fireCall);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onPhoneCallClick != null)
                        onPhoneCallClick.onLongClick(imgSelected, view, fireCall);
                    return true;
                }
            });

            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onPhoneCallClick != null)
                        onPhoneCallClick.onIconButtonClick(view, fireCall);
                }
            });


        }


        private Drawable getPhoneCallType(int type) {
            Drawable incomingDrawable = context.getResources().getDrawable(R.drawable.ic_call_received);
            Drawable outgoingDrawable = context.getResources().getDrawable(R.drawable.ic_call_made);

            switch (type) {
                case FireCallType.OUTGOING:
                    outgoingDrawable.mutate();
                    DrawableCompat.setTintMode(outgoingDrawable, PorterDuff.Mode.SRC_IN);
                    DrawableCompat.setTint(outgoingDrawable, context.getResources().getColor(R.color.colorGreen));
                    return outgoingDrawable;

                case FireCallType.ANSWERED:
                    incomingDrawable.mutate();
                    DrawableCompat.setTintMode(incomingDrawable, PorterDuff.Mode.SRC_IN);
                    DrawableCompat.setTint(incomingDrawable, context.getResources().getColor(R.color.colorGreen));
                    return incomingDrawable;

                default:
                    incomingDrawable.mutate();
                    DrawableCompat.setTintMode(incomingDrawable, PorterDuff.Mode.SRC_IN);
                    DrawableCompat.setTint(incomingDrawable, context.getResources().getColor(R.color.red));
                    return incomingDrawable;


            }
        }
    }


    public interface OnClickListener {
        void onItemClick(HidelyImageView hidelyImageView, View itemView, FireCall fireCall);

        void onIconButtonClick(View itemView, FireCall fireCall);

        void onLongClick(HidelyImageView hidelyImageView, View itemView, FireCall fireCall);
    }

    public void filter(String query) {
        if (query.trim().isEmpty()) {
            fireCallList = originalList;
        } else {
            RealmResults<FireCall> fireCalls = RealmHelper.getInstance().searchForCall(query);
            fireCallList = fireCalls;
        }

        notifyDataSetChanged();
    }
}
