package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ONE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_USERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bytesbee.firebase.chat.activities.fragments.ChatsFragment;
import com.bytesbee.firebase.chat.activities.fragments.GroupsFragment;
import com.bytesbee.firebase.chat.activities.fragments.ProfileFragment;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.CustomTypefaceSpan;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    private CircleImageView mImageView;
    private TextView mTxtUsername;
    private ViewPager2 mViewPager;
    private long exitTime = 0;
    private FloatingActionButton fabMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mTxtUsername = findViewById(R.id.txtUsername);
        final Toolbar mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("");

        final AdView adView = findViewById(R.id.adView);
        if (BuildConfig.ADS_SHOWN) {
            adView.setVisibility(View.VISIBLE);
            final AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.hasChildren()) {
                        final User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        mTxtUsername.setText(user.getUsername());
                        if (user.getGenders() == GEN_UNSPECIFIED) {
                            Utils.selectGenderPopup(mActivity, firebaseUser.getUid(), GEN_UNSPECIFIED);
                        }

                        Utils.setProfileImage(getApplicationContext(), user.getMyImg(), mImageView);
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mImageView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                mViewPager.setCurrentItem(2);
            }
        });

        final TabLayout mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);
        fabMain = findViewById(R.id.fabMain);

        final ViewPageAdapter viewPageAdapter = new ViewPageAdapter(this);
        viewPageAdapter.addFragment(new ChatsFragment(), getString(R.string.strChats));
        viewPageAdapter.addFragment(new GroupsFragment(), getString(R.string.strGroups));
        viewPageAdapter.addFragment(new ProfileFragment(), getString(R.string.strProfile));

        mViewPager.setAdapter(viewPageAdapter);

        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> tab.setText(viewPageAdapter.getTitle(position))).attach();

        mViewPager.setOffscreenPageLimit(viewPageAdapter.getItemCount() - 1);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fabMain.setVisibility(View.VISIBLE);
                if (tab.getPosition() == ZERO) {
                    fabMain.setImageResource(R.drawable.ic_chat);
                    rotateFabForward();
                } else if (tab.getPosition() == ONE) {
                    fabMain.setImageResource(R.drawable.ic_group_add);
                    rotateFabForward();
                } else {
                    fabMain.setVisibility(View.GONE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fabMain.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                if (mViewPager.getCurrentItem() == ZERO) {
                    screens.showCustomScreen(UsersActivity.class);
                } else if (mViewPager.getCurrentItem() == ONE) {
                    screens.showCustomScreen(GroupsAddActivity.class);
                }
            }
        });

    }

    public void rotateFabForward() {
        ViewCompat.animate(fabMain)
                .rotation(5.0F)
                .withLayer()
                .setDuration(300L)
                .setInterpolator(new OvershootInterpolator(10.0F))
                .start();
        final Handler handler = new Handler(Looper.getMainLooper());
        //Write whatever to want to do after delay specified (1 sec)
        handler.postDelayed(this::rotateFabBackward, 200);
    }

    public void rotateFabBackward() {

        ViewCompat.animate(fabMain)
                .rotation(0.0F)
                .withLayer()
                .setDuration(300L)
                .setInterpolator(new OvershootInterpolator(10.0F))
                .start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class ViewPageAdapter extends FragmentStateAdapter {

        public final ArrayList<Fragment> fragments;
        public final ArrayList<String> titles;

        public ViewPageAdapter(@NonNull @NotNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments = new ArrayList<>();
            titles = new ArrayList<>();
        }

        @NonNull
        @NotNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        public String getTitle(int index) {
            return titles.get(index);
        }
    }

    public static void applyFontToMenu(Menu m, Context mContext) {
        for (int i = 0; i < m.size(); i++) {
            applyFontToMenuItem(m.getItem(i), mContext);
        }
    }

    public static void applyFontToMenuItem(MenuItem mi, Context mContext) {
        final SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", Utils.getRegularFont(mContext)), ZERO, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        applyFontToMenu(menu, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.itemSettings) {
            screens.openSettingsActivity();
            return true;
        } else if (itemId == R.id.itemLogout) {
            Utils.logout(mActivity);
            return true;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        try {
            if (mViewPager.getCurrentItem() == ZERO) {
                int DEFAULT_DELAY = 2000;
                if ((System.currentTimeMillis() - exitTime) > DEFAULT_DELAY) {
                    try {
                        final CoordinatorLayout mainRootLayout = findViewById(R.id.mainRootLayout);
                        final Snackbar snackbar = Snackbar.make(mainRootLayout, getString(R.string.press_again_to_exit), Snackbar.LENGTH_LONG);
                        final View sbView = snackbar.getView();
                        final TextView textView = sbView.findViewById(R.id.snackbar_text);
                        textView.setTypeface(Utils.getRegularFont(mActivity));
                        textView.setTextColor(Color.YELLOW);
                        snackbar.show();
                    } catch (Exception e) {
                        screens.showToast(R.string.press_again_to_exit);
                    }
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            } else {
                mViewPager.setCurrentItem(ZERO);
            }
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.readStatus(STATUS_ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
