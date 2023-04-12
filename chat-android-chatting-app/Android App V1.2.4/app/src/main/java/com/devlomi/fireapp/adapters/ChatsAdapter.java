package com.devlomi.fireapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import com.devlomi.fireapp.activities.ChatActivity;
import com.devlomi.fireapp.activities.main.MainActivity;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.constants.TypingStat;
import com.devlomi.fireapp.model.realms.Chat;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.AdapterHelper;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MessageTypeHelper;
import com.devlomi.fireapp.utils.RealmHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Devlomi on 03/08/2017.
 */

//the RealmRecyclerViewAdapter provides autoUpdate feature
//which will handle changes in list automatically with smooth animations
public class ChatsAdapter extends RealmRecyclerViewAdapter<Chat, RecyclerView.ViewHolder> {
    private Context context;
    //chats list
    List<Chat> originalList;
    List<Chat> chatList;

    //this list will contain the selected chats when user start selecting chats
    List<Chat> selectedChatForActionMode = new ArrayList<>();

    //this hashmap is to save typing state when user scrolls
    //because the recyclerView will not save it
    HashMap<String, Integer> typingStatHashmap = new HashMap<>();

    //get instance of activity so we can call the methods in it
    MainActivity activity;


    public ChatsAdapter(@Nullable OrderedRealmCollection<Chat> data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        this.originalList = data;
        this.context = context;
        chatList = data;
        activity = (MainActivity) context;
    }

    public List<Chat> getSelectedChatForActionMode() {
        return selectedChatForActionMode;
    }

    public HashMap<String, Integer> getTypingStatHashmap() {
        return typingStatHashmap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chats, parent, false);
        return new ChatsHolder(row);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Chat chat = chatList.get(position);
        final User user = chat.getUser();
        final ChatsHolder mHolder = (ChatsHolder) holder;


        //if other user is typing then show typing layout
        //this will set the state over scrolling
        if (typingStatHashmap.containsValue(chat.getChatId())) {
            mHolder.tvTypingStat.setVisibility(View.VISIBLE);
            mHolder.tvLastMessage.setVisibility(View.GONE);
            mHolder.countUnreadBadge.setVisibility(View.GONE);

            int stat = typingStatHashmap.get(chat.getChatId());
            if (stat == TypingStat.TYPING)
                if (stat == TypingStat.TYPING)
                    mHolder.tvTypingStat.setText(context.getResources().getString(R.string.typing));
                else if (stat == TypingStat.RECORDING)
                    mHolder.tvTypingStat.setText(context.getResources().getString(R.string.recording));


            //otherwise set default state and show last message layout
        } else {
            mHolder.tvTypingStat.setVisibility(View.GONE);
            mHolder.tvLastMessage.setVisibility(View.VISIBLE);
            mHolder.countUnreadBadge.setVisibility(View.VISIBLE);
        }


        keepActionModeItemsSelected(holder.itemView, chat);


        //set the user name from phonebook
        if (user != null && user.getPhone() != null) {
            mHolder.tvTitle.setText(user.getUserName());
        }


        //get the lastmessage from chat
        final Message message = chat.getLastMessage();
        //set last message time
        mHolder.timeChats.setText(chat.getTime());

        if (message != null) {

            final String content = message.getContent();
            //if it's a TextMessage
            if (message.isTextMessage() || message.getType() == MessageType.GROUP_EVENT || MessageType.isDeletedMessage(message.getType())) {
                //set group event text
                if (message.getType() == MessageType.GROUP_EVENT) {
                    String groupEvent = GroupEvent.extractString(message.getContent(), user.getGroup().getUsers());
                    mHolder.tvLastMessage.setText(groupEvent);
                    //set message deleted event text
                } else if (MessageType.isDeletedMessage(message.getType())) {
                    if (message.getType() == MessageType.SENT_DELETED_MESSAGE) {
                        mHolder.tvLastMessage.setText(context.getResources().getString(R.string.you_deleted_this_message));
                    } else {
                        mHolder.tvLastMessage.setText(context.getResources().getString(R.string.this_message_was_deleted));
                    }
                } else
                    //set the message text
                    mHolder.tvLastMessage.setText(content);
                //remove the icon besides the text (camera,voice etc.. icon)
                mHolder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);


                //Set Icon if it's not a Text Message
            } else {
                mHolder.tvLastMessage.setText(MessageTypeHelper.extractMessageTypeMetadataText(message));

                //set icon besides the type text
                Drawable drawable = getColoredDrawable(message);
                if (drawable != null) {
                    mHolder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    mHolder.tvLastMessage.setCompoundDrawablePadding(5);
                }
            }

            //Set Recipient Marks
            //if the Message was sent by user
            if (message.getType() == MessageType.GROUP_EVENT || MessageType.isDeletedMessage(message.getType())) {
                mHolder.imgReadTagChats.setVisibility(View.GONE);
            } else if (message.getFromId().equals(FireManager.getUid())) {
                mHolder.imgReadTagChats.setVisibility(View.VISIBLE);
                mHolder.imgReadTagChats.setImageDrawable(AdapterHelper.getColoredStatDrawable(context, message.getMessageStat()));
            } else {
                mHolder.imgReadTagChats.setVisibility(View.GONE);
            }
        } else {
            mHolder.tvLastMessage.setText("");
            mHolder.imgReadTagChats.setVisibility(View.GONE);
            mHolder.tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        int unreadCount = chat.getUnReadCount();

        //if there are unread messages hide the unread count badge
        if (unreadCount == 0)
            mHolder.countUnreadBadge.setVisibility(View.GONE);
            //otherwise show it and set the unread count
        else {
            mHolder.countUnreadBadge.setVisibility(View.VISIBLE);
            mHolder.countUnreadBadge.setText(chat.getUnReadCount() + "");
        }


        //on chat click
        mHolder.rlltBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if isInAction mode then select or remove the clicked chat from selectedActionList
                if (isInActionMode()) {
                    //if it's selected ,remove it
                    if (selectedChatForActionMode.contains(chat))
                        itemRemoved(holder.itemView, chat);

                        //otherwise add it to list
                    else
                        itemAdded(holder.itemView, chat);
                    //if it's not in actionMode start the chatActivity
                } else {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra(IntentUtils.UID, user.getUid());
                    context.startActivity(intent);
                }
            }
        });

        //start action mode and select this chat
        mHolder.rlltBody.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClicked(holder.itemView, holder.getAdapterPosition());

                return true;
            }
        });

        //show user profile in the dialog-like activity
        mHolder.userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.userProfileClicked(user);
            }
        });


        loadUserPhoto(user, mHolder.userProfile);


    }

    //change the icon color drawable depending on message state
    private Drawable getColoredDrawable(Message message) {
        int messageTypeResource = MessageTypeHelper.getMessageTypeDrawable(message.getType());
        if (messageTypeResource == -1) return null;
        Resources resources = context.getResources();

        Drawable drawable = resources.getDrawable(messageTypeResource);
        drawable.mutate();
        int color;


        //if it's a voice message
        if (message.isVoiceMessage()) {
            //if it was sent by the user
            if (message.getType() == MessageType.SENT_VOICE_MESSAGE) {
                //if the other user listened to it set the color to blue
                if (message.isVoiceMessageSeen()) {
                    color = resources.getColor(R.color.colorBlue);
                } else {
                    //if it's not listened set it to grey
                    color = resources.getColor(R.color.colorTextDesc);
                }
                //if this message is received from the other user
            } else {
                //if the user listened to it set it to blue
                if (message.isVoiceMessageSeen()) {
                    color = resources.getColor(R.color.colorBlue);
                    //otherwise set it to green
                } else {
                    color = resources.getColor(R.color.colorGreen);
                }
            }
            //if it's not a voice message change the icon color to grey
        } else {
            color = resources.getColor(R.color.colorTextDesc);
        }
        //change the icon color
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public class ChatsHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlltBody;
        private ImageView userProfile;

        public TextView tvTitle, tvLastMessage, timeChats, tvTypingStat, countUnreadBadge;

        public ImageView imgReadTagChats;


        public ChatsHolder(View itemView) {
            super(itemView);
            rlltBody = itemView.findViewById(R.id.container_layout);
            userProfile = itemView.findViewById(R.id.user_photo);
            tvTitle = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            timeChats = itemView.findViewById(R.id.time_chats);
            imgReadTagChats = itemView.findViewById(R.id.img_read_tag_chats);
            countUnreadBadge = itemView.findViewById(R.id.count_unread_badge);

            tvTypingStat = itemView.findViewById(R.id.tv_typing_stat);

        }
    }


    private void loadUserPhoto(final User user, final ImageView imageView) {
        if (user == null)
            return;
        if (user.getUid() == null)
            return;

        if (user.isBroadcastBool())
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_broadcast_with_bg));
        else if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(context).load(bytes).asBitmap().into(imageView);
        }

        //if it's a broadcast there is no photo to load
        if (!user.isBroadcastBool()) {
            //check for a new thumb img
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


    //start action mode and select this chat
    private void onLongClicked(View view, int pos) {
        if (!isInActionMode()) {
            itemAdded(view, originalList.get(pos));
        }
        activity.onActionModeStarted();


    }

    private boolean isInActionMode() {
        return activity.isInActionMode();
    }

    private void itemAdded(View view, Chat chat) {
        //add chat to list
        selectedChatForActionMode.add(chat);
        //change background color to blue
        setBackgroundColor(view, true);
        //notify the activity and update toolbar text with the items count
        activity.addItemToActionMode(selectedChatForActionMode.size());


    }

    //set the background color on scroll because of default behavior of recyclerView
    private void keepActionModeItemsSelected(View itemView, Chat chat) {
        if (selectedChatForActionMode.contains(chat)) {
            setBackgroundColor(itemView, true);
        } else {
            setBackgroundColor(itemView, false);
        }
    }


    //remove chat from selected list
    private void itemRemoved(View view, Chat chat) {
        //change the background color to default color
        setBackgroundColor(view, false);
        //remove item from list
        selectedChatForActionMode.remove(chat);
        //notify the activity and update toolbar text with the items count
        activity.addItemToActionMode(selectedChatForActionMode.size());
        //if this is the last item then exit action mode
        if (selectedChatForActionMode.isEmpty()) {
            activity.exitActionMode();
        }
    }


    //exit action mode and notify the adapter to redraw the default items
    public void exitActionMode() {
        selectedChatForActionMode.clear();
        notifyDataSetChanged();
    }


    private void setBackgroundColor(View view, boolean isAdded) {
        if (isAdded)
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.item_selected_background_color));
        else
            //default background color
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.chats_background));



    }

    public void filter(String query) {
        if (query.trim().isEmpty()) {
            chatList = originalList;
        } else {
            RealmResults<Chat> chats = RealmHelper.getInstance().searchForChat(query);
            chatList = chats;
        }

        notifyDataSetChanged();
    }

}
