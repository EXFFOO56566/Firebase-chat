package com.devlomi.fireapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.adapters.NewCallAdapter;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.PerformCall;
import com.devlomi.fireapp.utils.RealmHelper;

import io.realm.RealmResults;

public class NewCallActivity extends AppCompatActivity implements NewCallAdapter.OnClickListener {
    private RecyclerView rvNewCall;
    NewCallAdapter adapter;
    private RealmResults<User> userList;
    private boolean isInSearchMode = false;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_call);
        rvNewCall = findViewById(R.id.rv_new_call);

        getSupportActionBar().setTitle(R.string.select_contact);
        //enable arrow item in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userList = RealmHelper.getInstance().getListOfUsers();

        adapter = new NewCallAdapter(userList, true, this);
        rvNewCall.setLayoutManager(new LinearLayoutManager(this));
        rvNewCall.setAdapter(adapter);
        adapter.setOnUserClick(this);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_call, menu);
        MenuItem menuItem = menu.findItem(R.id.search_item);
        searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, false);
                    adapter = new NewCallAdapter(users, true, NewCallActivity.this);
                    adapter.setOnUserClick(NewCallActivity.this);
                    rvNewCall.setAdapter(adapter);
                } else {
                    adapter = new NewCallAdapter(userList, true, NewCallActivity.this);
                    adapter.setOnUserClick(NewCallActivity.this);
                    rvNewCall.setAdapter(adapter);
                }
                return false;
            }

        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isInSearchMode = false;
                adapter = new NewCallAdapter(userList, true, NewCallActivity.this);
                rvNewCall.setAdapter(adapter);
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onUserClick(View view, User user, boolean isVideo) {
        new PerformCall(NewCallActivity.this).performCall(isVideo, user.getUid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.search_item) {
            isInSearchMode = true;

            if (searchView.isIconified())
                searchView.onActionViewExpanded();

            searchView.requestFocus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isInSearchMode)
            exitSearchMode();
        else
            super.onBackPressed();
    }

    private void exitSearchMode() {
        isInSearchMode = false;
        searchView.onActionViewCollapsed();
        adapter.notifyDataSetChanged();
    }
}
