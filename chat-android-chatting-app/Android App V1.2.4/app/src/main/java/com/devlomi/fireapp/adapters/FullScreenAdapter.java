package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.devlomi.fireapp.fragments.ImageViewFragment;
import com.devlomi.fireapp.fragments.VideoViewFragment;
import com.devlomi.fireapp.model.realms.Message;

import java.util.List;

/**
 * Created by Devlomi on 16/09/2017.
 */

public class FullScreenAdapter extends FragmentStatePagerAdapter {
    //media list items
    private List<Message> items;
    private Context context;
    ImageViewFragment imageViewFragment;
    VideoViewFragment videoViewFragment;
    private int mStartingPosition;

    public FullScreenAdapter(FragmentManager fm, Context context, List<Message> items, int mStartingPosition) {
        super(fm);
        this.context = context;
        this.items = items;
        this.mStartingPosition = mStartingPosition;
    }

    @Override
    public int getCount() {
        return items.size();
    }


    @Override
    public Fragment getItem(int position) {
        Message message = items.get(position);

        //if this item is a Video create VideoFragment
        if (message.isVideo())
            return VideoViewFragment.create(context, items.get(position));

        //otherwise create ImageView Fragment
        return ImageViewFragment.create(context, items.get(position), position, mStartingPosition);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (items.get(position).isVideo()) {
            videoViewFragment = (VideoViewFragment) object;
        } else {
            imageViewFragment = (ImageViewFragment) object;
        }
    }

    public View getImage() {
        if (videoViewFragment != null) {
            return null;
        }
        return imageViewFragment.getImageView();

    }


}
