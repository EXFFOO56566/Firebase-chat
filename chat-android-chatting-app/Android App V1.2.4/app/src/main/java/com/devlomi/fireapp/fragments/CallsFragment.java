package com.devlomi.fireapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.adapters.CallsAdapter;
import com.devlomi.fireapp.interfaces.FragmentCallback;
import com.devlomi.fireapp.model.realms.FireCall;
import com.devlomi.fireapp.utils.PerformCall;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.hidely.hidelyviews.HidelyImageView;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;


public class CallsFragment extends BaseFragment implements ActionMode.Callback, CallsAdapter.OnClickListener {
    private RecyclerView rvCalls;
    AdView adView;

    private RealmResults<FireCall> fireCallList;
    private List<FireCall> selectedFireCallListActionMode = new ArrayList<>();

    private CallsAdapter adapter;
    FragmentCallback listener;
    ActionMode actionMode;

    @Override
    public boolean showAds() {
        return getResources().getBoolean(R.bool.is_calls_ad_enabled);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentCallback) context;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calls, container, false);
        rvCalls = view.findViewById(R.id.rv_calls);
        adView = view.findViewById(R.id.ad_view);
        adViewInitialized(adView);
        initAdapter();


        return view;
    }


    private void initAdapter() {
        fireCallList = RealmHelper.getInstance().getAllCalls();
        adapter = new CallsAdapter(fireCallList, selectedFireCallListActionMode, getActivity(), CallsFragment.this);
        rvCalls.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCalls.setAdapter(adapter);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;
        actionMode.getMenuInflater().inflate(R.menu.menu_action_calls, menu);
        actionMode.setTitle("1");
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (actionMode != null && menuItem != null) {
            if (menuItem.getItemId() == R.id.menu_item_delete)
                deleteClicked();
        }
        return true;
    }

    private void deleteClicked() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.confirmation);
        dialog.setMessage(R.string.delete_calls_confirmation);
        dialog.setNegativeButton(R.string.no, null);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (FireCall fireCall : selectedFireCallListActionMode) {
                    RealmHelper.getInstance().deleteCall(fireCall);
                }
                exitActionMode();
            }
        });
        dialog.show();

    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        selectedFireCallListActionMode.clear();
        adapter.notifyDataSetChanged();
    }

    private void itemRemovedFromActionList(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {

        selectedFireCallListActionMode.remove(fireCall);
        if (selectedFireCallListActionMode.isEmpty()) {
            actionMode.finish();
        } else {
            selectedCircle.hide();
            itemView.setBackgroundColor(-1);
            actionMode.setTitle(selectedFireCallListActionMode.size() + "");
        }


    }

    private void itemAddedToActionList(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        selectedCircle.show();
        itemView.setBackgroundColor(getResources().getColor(R.color.light_blue));
        selectedFireCallListActionMode.add(fireCall);
        actionMode.setTitle(selectedFireCallListActionMode.size() + "");
    }

    public boolean isActionModeNull() {
        return actionMode == null;
    }

    public void exitActionMode() {

        if (actionMode != null)
            actionMode.finish();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && actionMode != null)
            actionMode.finish();
    }

    @Override
    public void onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
        if (adapter != null) {
            adapter.filter(newText);
        }
    }

    @Override
    public void onSearchClose() {
        super.onSearchClose();
        adapter = new CallsAdapter(fireCallList, selectedFireCallListActionMode, getActivity(), CallsFragment.this);
        rvCalls.setAdapter(adapter);
    }


    @Override
    public void onItemClick(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        if (actionMode != null) {
            if (selectedFireCallListActionMode.contains(fireCall))
                itemRemovedFromActionList(selectedCircle, itemView, fireCall);
            else
                itemAddedToActionList(selectedCircle, itemView, fireCall);
        } else if (fireCall.getUser() != null && fireCall.getUser().getUid() != null)
            new PerformCall(getActivity()).performCall(fireCall.isVideo(), fireCall.getUser().getUid());
    }

    @Override
    public void onIconButtonClick(View view, FireCall fireCall) {
        if (actionMode != null) return;

        if (fireCall.getUser() != null && fireCall.getUser().getUid() != null)
            new PerformCall(getActivity()).performCall(fireCall.isVideo(), fireCall.getUser().getUid());
    }

    @Override
    public void onLongClick(HidelyImageView selectedCircle, View itemView, FireCall fireCall) {
        if (actionMode == null) {
            fragmentCallback.startTheActionMode(CallsFragment.this);
            itemAddedToActionList(selectedCircle, itemView, fireCall);
        }
    }
}
