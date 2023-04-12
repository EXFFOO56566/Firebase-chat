package com.bytesbee.firebase.chat.activities.adapters;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EMPTY;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_SEEN;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ONE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_CHATS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SLASH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_AUDIO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_DOCUMENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_IMAGE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_LOCATION;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_RECORDING;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_VIDEO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Chat;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UserAdapters extends RecyclerView.Adapter<UserAdapters.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final Context mContext;
    private final ArrayList<User> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private boolean isMsgSeen = false;
    private int unReadCount = 0;
    private final Screens screens;

    public UserAdapters(Context mContext, ArrayList<User> usersList, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = Utils.removeDuplicates(usersList);
        this.isChat = isChat;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        screens = new Screens(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_users, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        final String strAbout = user.getAbout();

        viewHolder.txtUsername.setText(user.getUsername());
        Utils.setProfileImage(mContext, user.getImageURL(), viewHolder.imageView);

        viewHolder.txtLastMsg.setVisibility(View.VISIBLE);
        viewHolder.imgPhoto.setVisibility(View.GONE);
        if (isChat) {
            viewHolder.txtUnreadCounter.setVisibility(View.INVISIBLE);

            lastMessage(user.getId(), viewHolder.txtLastMsg, viewHolder.txtLastDate, viewHolder.imgPhoto);

            lastMessageCount(user.getId(), viewHolder.txtUnreadCounter);

            viewHolder.txtLastDate.setVisibility(View.VISIBLE);
        } else {
            viewHolder.txtUnreadCounter.setVisibility(View.GONE);
            viewHolder.txtLastDate.setVisibility(View.GONE);

            if (Utils.isEmpty(strAbout)) {
                viewHolder.txtLastMsg.setText(mContext.getString(R.string.strAboutStatus));
            } else {
                viewHolder.txtLastMsg.setText(strAbout);
            }
        }

        if (user.getIsOnline() == STATUS_ONLINE) {
            viewHolder.imgOn.setVisibility(View.VISIBLE);
            viewHolder.imgOff.setVisibility(View.GONE);
        } else {
            viewHolder.imgOn.setVisibility(View.GONE);
            viewHolder.imgOff.setVisibility(View.VISIBLE);
        }

        viewHolder.imageView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openProfilePictureActivity(user);
            }
        });

        viewHolder.itemView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openUserMessageActivity(user.getId());
            }
        });

        viewHolder.itemView.setOnLongClickListener(v -> {
            if (isChat) {
                Utils.setVibrate(mContext);
                final String receiverId = user.getId();
                final String currentUser = firebaseUser.getUid();

                Utils.showYesNoDialog(((Activity) mContext), R.string.strDelete, R.string.strDeleteConversion, () -> {
                    Query queryCurrent = FirebaseDatabase.getInstance().getReference().child(REF_CHATS).child(currentUser + SLASH + receiverId);
                    queryCurrent.getRef().removeValue();
                    Query queryReceiver = FirebaseDatabase.getInstance().getReference().child(REF_CHATS).child(receiverId + SLASH + currentUser);
                    queryReceiver.getRef().removeValue();
                });

            }
            return true;
        });
    }

    private void lastMessage(final String userId, final TextView lastMsg, final TextView lastDate, final ImageView imgPath) {

        theLastMsg = "default";
        txtLastDate = "Now";

        try {
            Query reference = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(firebaseUser.getUid() + SLASH + userId).limitToLast(1);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            final Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            try {
                                if (Utils.isEmpty(chat.getAttachmentType())) {
                                    if (!Utils.isEmpty(chat.getMessage())) {
                                        theLastMsg = chat.getMessage();
                                        txtLastDate = chat.getDatetime();
                                    }
                                } else {
                                    imgPath.setVisibility(View.VISIBLE);
                                    if (chat.getAttachmentType().equalsIgnoreCase(TYPE_IMAGE)) {
                                        theLastMsg = mContext.getString(R.string.lblImage);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_photo);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_AUDIO)) {
                                        theLastMsg = mContext.getString(R.string.lblAudio);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_audio);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_VIDEO)) {
                                        theLastMsg = mContext.getString(R.string.lblVideo);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_video);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_DOCUMENT)) {
                                        theLastMsg = mContext.getString(R.string.lblDocument);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_document);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_CONTACT)) {
                                        theLastMsg = mContext.getString(R.string.lblContact);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_contact);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_LOCATION)) {
                                        theLastMsg = mContext.getString(R.string.lblLocation);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_location);
                                    } else if (chat.getAttachmentType().equalsIgnoreCase(TYPE_RECORDING)) {
                                        theLastMsg = mContext.getString(R.string.lblVoiceRecording);
                                        txtLastDate = chat.getDatetime();
                                        imgPath.setImageResource(R.drawable.ic_small_recording);
                                    } else {
                                        imgPath.setVisibility(View.GONE);
                                        theLastMsg = chat.getMessage();
                                        txtLastDate = chat.getDatetime();
                                    }

                                }

                            } catch (Exception ignored) {
                            }
                        }

                        if ("default".equals(theLastMsg)) {
                            lastMsg.setText(R.string.msgNoMessage);
                            lastDate.setText(EMPTY);
                        } else {
                            lastMsg.setText(theLastMsg);
                            try {
                                lastDate.setText(Utils.formatDateTime(mContext, txtLastDate));
                            } catch (Exception ignored) {
                            }
                        }

                        theLastMsg = "default";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ignored) {

        }
    }

    private void lastMessageCount(final String userId, final TextView txtUnreadCounter) {

        isMsgSeen = false;
        unReadCount = 0;

        try {
            Query reference = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(firebaseUser.getUid() + SLASH + userId).orderByChild(EXTRA_SEEN).equalTo(false);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            try {
                                if (!Utils.isEmpty(chat.getMessage())) {
                                    if (chat.getSender().equalsIgnoreCase(firebaseUser.getUid())) {
                                        isMsgSeen = true;
                                    } else {
                                        isMsgSeen = chat.isMsgseen();
                                        if (!isMsgSeen) {
                                            unReadCount++;
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (isMsgSeen || unReadCount == ZERO) {
                        txtUnreadCounter.setVisibility(View.INVISIBLE);
                    } else {
                        final String readCount = unReadCount > 99 ? "99+" : String.valueOf(unReadCount);
                        txtUnreadCounter.setVisibility(View.VISIBLE);
                        txtUnreadCounter.setText(readCount);
                    }
                    unReadCount = 0;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ignored) {

        }
    }

    @NonNull
    @NotNull
    @Override
    public String getSectionName(final int position) {
        if (!Utils.isEmpty(mUsers)) {
            return mUsers.get(position).getUsername().substring(ZERO, ONE);
        } else {
            return null;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final ImageView imageView;
        public final TextView txtUsername;
        private final ImageView imgOn;
        private final ImageView imgOff;
        private final TextView txtLastMsg;
        private final TextView txtLastDate;
        private final TextView txtUnreadCounter;
        private final ImageView imgPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            imgOn = itemView.findViewById(R.id.imgOn);
            imgOff = itemView.findViewById(R.id.imgOff);
            txtLastMsg = itemView.findViewById(R.id.txtLastMsg);
            txtLastDate = itemView.findViewById(R.id.txtLastDate);
            txtUnreadCounter = itemView.findViewById(R.id.txtUnreadCounter);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
