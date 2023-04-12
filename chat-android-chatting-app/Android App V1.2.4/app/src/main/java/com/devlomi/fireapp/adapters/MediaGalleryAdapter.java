package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.FullscreenActivity;
import com.devlomi.fireapp.activities.MediaGalleryActivity;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Devlomi on 23/11/2017.
 */

public class MediaGalleryAdapter extends RecyclerView.Adapter {
    Context context;
    List<Message> mediaList;
    MediaGalleryActivity activity;
    //selected gallery items by the user
    List<Message> selectedItems = new ArrayList<>();

    public MediaGalleryAdapter(Context context, List<Message> imagesList) {
        this.context = context;
        this.mediaList = imagesList;
        activity = (MediaGalleryActivity) context;
    }

    public List<Message> getSelectedItems() {
        return selectedItems;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gallery, parent, false);
        return new GalleryHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GalleryHolder mHolder = (GalleryHolder) holder;

        final Message image = mediaList.get(position);

        final String path = image.getLocalPath();
        final String messageId = image.getMessageId();

        keepItemsSelected(mHolder, image);

        //if it's an Image load the Image
        if (!image.isVideo()) {
            Glide.with(context).load(path).placeholder(R.drawable.image_placeholder).into(mHolder.imageViewGallery);
            //hide video info layout
            mHolder.layoutVideoInfoGallery.setVisibility(View.GONE);
        } else {
            //show video layout info
            mHolder.layoutVideoInfoGallery.setVisibility(View.VISIBLE);

            //get video thumbnail
            byte[] videoThumb = BitmapUtils.encodeImageAsBytes(image.getVideoThumb());

            Glide.with(context).load(videoThumb).asBitmap().placeholder(R.drawable.image_placeholder).into(mHolder.imageViewGallery);

//            mHolder.imageViewGallery.setImageBitmap(videoThumb);
            String videoLength = image.getMediaDuration();
            //set video duration
            mHolder.tvVideoLength.setText(videoLength);
        }

        //on item click
        mHolder.frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInActionMode()) {
                    //if it's selected unselect
                    if (selectedItems.contains(image))
                        itemRemoved((FrameLayout) view, image);
                    else
                        //select
                        itemAdded((FrameLayout) view, image);

                } else {
                    //start view full screen
                    Intent intent = new Intent(context, FullscreenActivity.class);
                    intent.putExtra(IntentUtils.EXTRA_PATH, path);
                    intent.putExtra(IntentUtils.UID, activity.getUser().getUid());
                    intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
                    context.startActivity(intent);
                }
            }
        });

        //start action mode and select this item
        mHolder.frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isInActionMode()) {
                    itemAdded((FrameLayout) v, image);
                }

                activity.onActionModeStarted();

                return true;
            }
        });

    }

    //keep items selected when scroll
    private void keepItemsSelected(GalleryHolder mHolder, Message image) {
        if (selectedItems.contains(image))
            setForegroundOverlay(mHolder.frameLayout, true);
        else
            setForegroundOverlay(mHolder.frameLayout, false);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }


    //delete items
    //if checkbox is checked then delete the file from storage
    //otherwise just delete from database
    public void deleteItems(boolean isDeleteChecked) {
        for (Message message : selectedItems) {
            RealmHelper.getInstance().deleteMessageFromRealm(message.getChatId(), message.getMessageId(), isDeleteChecked);
        }

        //clear list
        selectedItems.clear();


    }

    class GalleryHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewGallery;
        private RelativeLayout layoutVideoInfoGallery;
        private TextView tvVideoLength;
        private FrameLayout frameLayout;


        public GalleryHolder(View itemView) {
            super(itemView);
            imageViewGallery = itemView.findViewById(R.id.image_view_gallery);
            layoutVideoInfoGallery = itemView.findViewById(R.id.layout_video_info_gallery);
            tvVideoLength = itemView.findViewById(R.id.tv_video_length);
            frameLayout = itemView.findViewById(R.id.frame_layout);


        }
    }

    private boolean isInActionMode() {
        return activity.isInActionMode();
    }

    private void itemAdded(FrameLayout frameLayout, Message message) {
        selectedItems.add(message);
        setForegroundOverlay(frameLayout, true);
        activity.addItemToActionMode(selectedItems.size());
    }


    private void itemRemoved(FrameLayout frameLayout, Message message) {
        setForegroundOverlay(frameLayout, false);
        selectedItems.remove(message);
        activity.addItemToActionMode(selectedItems.size());
        if (selectedItems.isEmpty()) {
            activity.exitActionMode();
        }
    }


    public void exitActionMode() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    private void setForegroundOverlay(FrameLayout view, boolean isAdded) {
        if (isAdded)
            view.setForeground(getForegroundDrawable());
        else
            view.setForeground(null);


    }

    private Drawable getForegroundDrawable() {
        return ContextCompat.getDrawable(context, R.drawable.check_image_overlay);
    }


}
