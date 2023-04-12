package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.FullscreenActivity;
import com.devlomi.fireapp.activities.MediaGalleryActivity;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.IntentUtils;

import java.util.List;

/**
 * Created by Devlomi on 23/11/2017.
 */

public class MediaHorizontalAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MEDIA = 0;
    private static final int VIEW_TYPE_ARROW = 1;
    private static final int MAXIMUM_ITEMS_TO_SHOW = 13;

    Context context;
    List<Message> mediaList;
    User user;

    public MediaHorizontalAdapter(Context context, List<Message> mediaList, User user) {
        this.context = context;
        this.mediaList = mediaList;
        this.user = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row;
        //if the view is the Arrow Button
        if (viewType == VIEW_TYPE_ARROW) {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_arrow, parent, false);
            return new ArrowHolder(row);

            //otherwise inflate image/video
        } else {
            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_media, parent, false);
            return new ImageHolder(row);
        }

    }

    @Override
    public int getItemViewType(int position) {
        //if it's the last item then return arrow type
        if (position == mediaList.size())
            return VIEW_TYPE_ARROW;

        //otherwise return image/video
        return VIEW_TYPE_MEDIA;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //if it's a Media type then inflate image/video
        if (getItemViewType(position) == VIEW_TYPE_MEDIA) {
            ImageHolder mHolder = (ImageHolder) holder;
            Message message = mediaList.get(position);
            final String path = message.getLocalPath();
            final String messageId = message.getMessageId();

            if (!message.isVideo()) {
                //show image
                Glide.with(context).load(path).placeholder(R.drawable.image_placeholder).into(mHolder.imageViewMedia);
                //hide videoInfo layout(video duration and video icon)
                mHolder.layoutVideoInfoGallery.setVisibility(View.GONE);


            } else {
                //if it's a video ,show video layout
                mHolder.layoutVideoInfoGallery.setVisibility(View.VISIBLE);
                byte[] videoThumb = BitmapUtils.encodeImageAsBytes(message.getVideoThumb());
//                mHolder.imageViewMedia.setImageBitmap(videoThumb);
                //show image thumb
                Glide.with(context).load(videoThumb).asBitmap().placeholder(R.drawable.image_placeholder).into(mHolder.imageViewMedia);
                String videoLength = message.getMediaDuration();
                //set video duration
                mHolder.tvVideoLength.setText(videoLength);

            }

            //on image/video click
            mHolder.imageViewMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FullscreenActivity.class);
                    intent.putExtra(IntentUtils.EXTRA_PATH, path);
                    intent.putExtra(IntentUtils.UID, user.getUid());
                    intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
                    context.startActivity(intent);
                }
            });
        } else {
            ArrowHolder mHolder = (ArrowHolder) holder;
            //on arrow click
            mHolder.btnArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MediaGalleryActivity.class);
                    intent.putExtra(IntentUtils.UID, user.getUid());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        //this "1" is for Arrow Button
        // if it's last item then inflate button to view more
        if (mediaList.size() > MAXIMUM_ITEMS_TO_SHOW)
            return mediaList.size() + 1;

        return mediaList.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewMedia;
        private RelativeLayout layoutVideoInfoGallery;
        private TextView tvVideoLength;


        public ImageHolder(View itemView) {
            super(itemView);
            imageViewMedia = itemView.findViewById(R.id.image_view_media);
            layoutVideoInfoGallery = itemView.findViewById(R.id.layout_video_info_gallery);
            tvVideoLength = itemView.findViewById(R.id.tv_video_length);

        }
    }

    class ArrowHolder extends RecyclerView.ViewHolder {
        private ImageButton btnArrow;

        public ArrowHolder(View itemView) {
            super(itemView);
            btnArrow = itemView.findViewById(R.id.btn_arrow);
        }
    }


}
