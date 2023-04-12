package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.StatusType;
import com.devlomi.fireapp.model.realms.Status;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.TimeHelper;
import com.devlomi.fireapp.views.TextViewWithShapeBackground;
import com.devlomi.hidely.hidelyviews.HidelyImageView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class MyStatusAdapter extends RealmRecyclerViewAdapter<Status, MyStatusAdapter.StatusHolder> {

    private List<Status> myStatusList;
    private List<Status> selectedStatusForActionMode;
    private Context context;
    private OnClickListener onStatusClick;


    public MyStatusAdapter(@Nullable OrderedRealmCollection<Status> data, List<Status> selectedStatusForActionMode, Context context) {
        super(data, true);
        this.myStatusList = data;
        this.selectedStatusForActionMode = selectedStatusForActionMode;
        this.context = context;
    }

    public void setOnStatusClick(OnClickListener onStatusClick) {
        this.onStatusClick = onStatusClick;
    }


    @NonNull
    @Override
    public StatusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_my_status, parent, false);
        return new StatusHolder(row);
    }


    @Override
    public void onBindViewHolder(@NonNull StatusHolder holder, int position) {
        holder.bind(myStatusList.get(position));
    }

    @Override
    public int getItemCount() {
        return myStatusList.size();
    }

    class StatusHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView tvStatusTime;
        private TextView tvStatusSeenCount;
        private HidelyImageView imgSelected;
        private TextViewWithShapeBackground tvTextStatus;


        public StatusHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            tvStatusTime = itemView.findViewById(R.id.tv_status_time);
            imgSelected = itemView.findViewById(R.id.img_selected);
            tvStatusSeenCount = itemView.findViewById(R.id.tv_status_seen_count);
            tvTextStatus = itemView.findViewById(R.id.tv_text_status);

        }

        public void bind(final Status status) {

            if (selectedStatusForActionMode.contains(status)) {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
                imgSelected.setVisibility(View.VISIBLE);

            } else {
                itemView.setBackgroundColor(-1);
                imgSelected.setVisibility(View.INVISIBLE);
            }


            tvStatusTime.setText(TimeHelper.getStatusTime(status.getTimestamp()));
            tvStatusSeenCount.setText(status.getSeenCount() + "");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onStatusClick != null)
                        onStatusClick.onStatusClick(view, imgSelected, status);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (onStatusClick != null)
                        onStatusClick.onStatusLongClick(view, imgSelected, status);
                    return true;
                }
            });


            if (status.getType() == StatusType.TEXT) {
                tvTextStatus.setVisibility(View.VISIBLE);
                profileImage.setVisibility(View.GONE);
                tvTextStatus.setText(status.getTextStatus().getText());
                tvTextStatus.setShapeColor(Color.parseColor(status.getTextStatus().getBackgroundColor()));
            } else {
                tvTextStatus.setVisibility(View.GONE);
                profileImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(BitmapUtils.encodeImageAsBytes(status.getThumbImg())).asBitmap().into(profileImage);

            }


        }
    }

    public interface OnClickListener {
        void onStatusClick(View view, HidelyImageView selectedCircle, Status status);

        void onStatusLongClick(View view, HidelyImageView selectedCircle, Status status);

    }
}
