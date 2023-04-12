package com.devlomi.fireapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.adapters.ChatsAdapter;
import com.devlomi.fireapp.model.constants.MessageStat;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.constants.TypingStat;
import com.devlomi.fireapp.model.realms.Chat;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.FireConstants;
import com.devlomi.fireapp.utils.FireListener;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupTyping;
import com.devlomi.fireapp.utils.RealmHelper;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

public class FragmentChats extends BaseFragment implements GroupTyping.GroupTypingListener {
    private RecyclerView rvChats;
    ChatsAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    RealmResults<Chat> chatList;
    OrderedRealmCollectionChangeListener<RealmResults<Chat>> changeListener;
    ValueEventListener typingEventListener, voiceMessageListener, lastMessageStatListener;

    List<GroupTyping> groupTypingList;
    FireListener fireListener;
    AdView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chats, container, false);
        init(view);
        fireListener = new FireListener();
        getChats();
        setTheAdapter();


        listenForTypingStat();
        listenForVoiceMessageStat();
        listenForLastMessageStat();
        listenForMessagesChanges();


        adViewInitialized(adView);


        return view;
    }

    @Override
    public boolean showAds() {
        return getResources().getBoolean(R.bool.is_calls_ad_enabled);
    }


    private void init(View view) {
        rvChats = view.findViewById(R.id.rv_chats);
        adView = view.findViewById(R.id.ad_view);
    }

    //add a listener for the last message if the user has replied from the notification
    private void listenForMessagesChanges() {
        changeListener = new OrderedRealmCollectionChangeListener<RealmResults<Chat>>() {
            @Override
            public void onChange(RealmResults<Chat> chats, OrderedCollectionChangeSet changeSet) {

                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();

                if (modifications.length != 0) {
                    Chat chat = chats.get(modifications[0].startIndex);
                    Message lastMessage = chat.getLastMessage();

                    if (lastMessage != null && lastMessage.getMessageStat() == MessageStat.PENDING
                            || lastMessage != null && lastMessage.getMessageStat() == MessageStat.SENT) {
                        addMessageStatListener(chat.getChatId(), lastMessage);
                    }
                }
            }
        };
    }

    //listen for lastMessage stat if it's received or read by the other user
    private void listenForLastMessageStat() {
        lastMessageStatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;

                int val = dataSnapshot.getValue(Integer.class);
                String key = dataSnapshot.getKey();
                String chatId = dataSnapshot.getRef().getParent().getKey();
                RealmHelper.getInstance().updateMessageStatLocally(key, chatId, val);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void addVoiceMessageStatListener() {
        for (Chat chat : chatList) {
            Message lastMessage = chat.getLastMessage();

            User user = chat.getUser();
            if (user==null)continue;
            if (!user.isBroadcastBool() && lastMessage != null && lastMessage.getType() != MessageType.GROUP_EVENT && lastMessage.isVoiceMessage()
                    && lastMessage.getFromId().equals(FireManager.getUid())
                    && !lastMessage.isVoiceMessageSeen()) {

                DatabaseReference reference = FireConstants.voiceMessageStat.child(lastMessage.getChatId()).child(lastMessage.getMessageId());
                fireListener.addListener(reference, voiceMessageListener);
            }
        }
    }

    private void addMessageStatListener() {
        for (Chat chat : chatList) {
            Message lastMessage = chat.getLastMessage();
            if (!chat.getUser().isBroadcastBool() && lastMessage != null && lastMessage.getType() != MessageType.GROUP_EVENT && lastMessage.getMessageStat() != MessageStat.READ) {
                DatabaseReference reference = FireConstants.messageStat.child(chat.getChatId()).child(lastMessage.getMessageId());
                fireListener.addListener(reference, lastMessageStatListener);
            }
        }
    }

    private void addMessageStatListener(String chatId, Message lastMessage) {
        if (lastMessage != null && lastMessage.getType() != MessageType.GROUP_EVENT && lastMessage.getMessageStat() != MessageStat.READ) {
            DatabaseReference reference = FireConstants.messageStat.child(chatId).child(lastMessage.getMessageId());
            fireListener.addListener(reference, lastMessageStatListener);
        }
    }

    //if the lastMessage is a Voice message then we want to
    //listen if it's listened by the other user
    private void listenForVoiceMessageStat() {
        voiceMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null) {
                    return;
                }

                String key = dataSnapshot.getKey();
                String chatId = dataSnapshot.getRef().getParent().getKey();
                RealmHelper.getInstance().updateVoiceMessageStatLocally(key, chatId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


    }

    //listen if other user is typing to this user
    private void listenForTypingStat() {
        typingEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;

                int stat = dataSnapshot.getValue(Integer.class);
                String uid = dataSnapshot.getRef().getParent().getKey();

                //create temp chat object to get the index of the uid
                Chat chat = new Chat();
                chat.setChatId(uid);
                int i = chatList.indexOf(chat);
                //if chat is not exists in the list return
                if (i == -1) return;

                ChatsAdapter.ChatsHolder vh = (ChatsAdapter.ChatsHolder) rvChats.findViewHolderForAdapterPosition(i);

                if (vh == null) {
                    return;
                }


                adapter.getTypingStatHashmap().put(chat.getChatId(), stat);
                TextView typingTv = vh.tvTypingStat;
                TextView lastMessageTv = vh.tvLastMessage;
                ImageView lastMessageReadIcon = vh.imgReadTagChats;


                //if other user is typing or recording to this user
                //then hide last message textView with all its contents
                if (stat == TypingStat.TYPING || stat == TypingStat.RECORDING) {
                    lastMessageTv.setVisibility(View.GONE);
                    lastMessageReadIcon.setVisibility(View.GONE);
                    typingTv.setVisibility(View.VISIBLE);

                    if (stat == TypingStat.TYPING)
                        typingTv.setText(getResources().getString(R.string.typing));
                    else if (stat == TypingStat.RECORDING)
                        typingTv.setText(getResources().getString(R.string.recording));

                    //in case there is no typing or recording event
                    //revert back to normal mode and show last message
                } else {
                    adapter.getTypingStatHashmap().remove(chat.getChatId());
                    typingTv.setVisibility(View.GONE);
                    lastMessageTv.setVisibility(View.VISIBLE);
                    Message lastMessage = chatList.get(i).getLastMessage();
                    if (lastMessage != null &&
                            lastMessage.getType() != MessageType.GROUP_EVENT
                            && !MessageType.isDeletedMessage(lastMessage.getType())
                            && lastMessage.getFromId().equals(FireManager.getUid())) {
                        lastMessageReadIcon.setVisibility(View.VISIBLE);
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


    }

    //adding typing listeners for all chats
    private void addTypingStatListener() {
        if (FireManager.getUid() == null)
            return;

        for (Chat chat : chatList) {
            User user = chat.getUser();
            if (user==null)continue;
            if (user.isGroupBool() && user.getGroup().isActive()) {

                if (groupTypingList == null)
                    groupTypingList = new ArrayList<>();

                GroupTyping groupTyping = new GroupTyping(user.getGroup().getUsers(), user.getUid(), this);
                groupTypingList.add(groupTyping);

            } else {
                String receiverUid = user.getUid();
                DatabaseReference typingStat = FireConstants.mainRef.child("typingStat").child(receiverUid)
                        .child(FireManager.getUid());
                fireListener.addListener(typingStat, typingEventListener);
            }
        }

    }

    private void getChats() {
        chatList = RealmHelper.getInstance().getAllChats();
    }


    private void setTheAdapter() {
        adapter = new ChatsAdapter(chatList, true, getActivity());
        linearLayoutManager = new LinearLayoutManager(getActivity());
        rvChats.setLayoutManager(linearLayoutManager);
        rvChats.setAdapter(adapter);
    }

    @Override
    public void onTyping(int state, String groupId, User user) {
        Chat tempChat = new Chat();
        tempChat.setChatId(groupId);
        int i = chatList.indexOf(tempChat);

        if (i == -1) return;
        if (user == null) return;
        Chat chat = chatList.get(i);


        ChatsAdapter.ChatsHolder vh = (ChatsAdapter.ChatsHolder) rvChats.findViewHolderForAdapterPosition(i);

        if (vh == null) {
            return;
        }


        adapter.getTypingStatHashmap().put(chat.getChatId(), state);
        TextView typingTv = vh.tvTypingStat;
        TextView lastMessageTv = vh.tvLastMessage;
        ImageView lastMessageReadIcon = vh.imgReadTagChats;


        //if other user is typing or recording to this user
        //then hide last message textView with all its contents
        if (state == TypingStat.TYPING || state == TypingStat.RECORDING) {
            lastMessageTv.setVisibility(View.GONE);
            lastMessageReadIcon.setVisibility(View.GONE);
            typingTv.setVisibility(View.VISIBLE);
            typingTv.setText(user.getUserName() + " is " + TypingStat.getStatString(getActivity(), state));
        }
    }

    @Override
    public void onAllNotTyping(String groupId) {


        Chat tempChat = new Chat();
        tempChat.setChatId(groupId);
        int i = chatList.indexOf(tempChat);

        if (i == -1) return;
        Chat chat = chatList.get(i);

        ChatsAdapter.ChatsHolder vh = (ChatsAdapter.ChatsHolder) rvChats.findViewHolderForAdapterPosition(i);

        if (vh == null) {
            return;
        }


        TextView typingTv = vh.tvTypingStat;
        TextView lastMessageTv = vh.tvLastMessage;
        ImageView lastMessageReadIcon = vh.imgReadTagChats;

        adapter.getTypingStatHashmap().remove(chat.getChatId());
        typingTv.setVisibility(View.GONE);
        lastMessageTv.setVisibility(View.VISIBLE);
        Message lastMessage = chatList.get(i).getLastMessage();
        if (lastMessage != null &&
                lastMessage.getType() != MessageType.GROUP_EVENT
                && !MessageType.isDeletedMessage(lastMessage.getType())
                && lastMessage.getFromId().equals(FireManager.getUid())) {
            lastMessageReadIcon.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        addTypingStatListener();
        addVoiceMessageStatListener();
        addMessageStatListener();
        chatList.addChangeListener(changeListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        fireListener.cleanup();
        if (groupTypingList != null) {
            for (GroupTyping groupTyping : groupTypingList) {
                groupTyping.cleanUp();
            }
        }
        chatList.removeChangeListener(changeListener);
    }


    public ChatsAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
        adapter.filter(newText);
    }

    @Override
    public void onSearchClose() {
        super.onSearchClose();
        adapter = new ChatsAdapter(chatList, true, getActivity());
        if (rvChats != null)
            rvChats.setAdapter(adapter);
    }


}
