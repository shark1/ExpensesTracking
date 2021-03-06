package pazzaglia.it.expensestracking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pazzaglia.it.expensestracking.R;
import pazzaglia.it.expensestracking.adapters.ExpensesListAdapter;
import pazzaglia.it.expensestracking.models.Expense;
import pazzaglia.it.expensestracking.models.ExpensesListPOJO;
import pazzaglia.it.expensestracking.network.AbstractApiCaller;
import pazzaglia.it.expensestracking.network.ExpensesCaller;
import pazzaglia.it.expensestracking.shared.Common;

public class LandingPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_EDIT = 0;
    public static final String MESSAGE = "MESSAGE";

    private List<Expense> data;
    private ExpensesListAdapter adapter;

    @Bind(R.id.toolbar) Toolbar _toolbar;
    @Bind(R.id.fab) FloatingActionButton _fab;
    @Bind(R.id.drawer_layout) DrawerLayout _drawer;
    @Bind(R.id.nav_view) NavigationView _navigationView;
    @Bind(R.id.card_recycler_view) RecyclerView _recycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing_page);
        ButterKnife.bind(this);

        setSupportActionBar(_toolbar);
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LandingPageActivity.this, ExpenseDetailActivity.class);
                intent.putExtra(ExpenseDetailActivity.MODE, ExpenseDetailActivity.ADD);
                startActivityForResult(intent,REQUEST_EDIT);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, _drawer, _toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        _drawer.setDrawerListener(toggle);
        toggle.syncState();
        _navigationView.setNavigationItemSelectedListener(this);

        //Set the name on the _drawer
        String name = Common.getName(this);
        ((TextView)_navigationView.getHeaderView(0).findViewById(R.id.textViewName)).setText(name);

        //Retrieve the data and show them
        expensesListLoading();
    }

    private void expensesListLoading(){
        //initialize the view
        _recycleView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        _recycleView.setLayoutManager(layoutManager);

        //Downaload the data
        downloadExpenses();
    }

    private void downloadExpenses(){
        //Retrofit expenses list download
        ExpensesCaller expensesCaller = new ExpensesCaller(this);
        expensesCaller.doApiCall(this, "Retrieving data...", new AbstractApiCaller.MyCallbackInterface<ExpensesListPOJO>() {
            @Override
            public void onDownloadFinishedOK(ExpensesListPOJO result) {
                data = result.getExpenses();
                Common.updateTotalExpenses(LandingPageActivity.this, data);
                adapter = new ExpensesListAdapter(data, LandingPageActivity.this);
                _recycleView.setAdapter(adapter);
            }
            @Override
            public void onDownloadFinishedKO(ExpensesListPOJO result) {
                onDownloadListFailure("Error downloading data");
            }

            @Override
            public void doApiCallOnFailure() {
                onDownloadListFailure("Please check your network connection and internet permission");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (_drawer.isDrawerOpen(GravityCompat.START)) {
            _drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            //reset API Key
            Common.clearApiKeyAndName(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();

        return true;
    }

    private void onDownloadListFailure(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), data.getStringExtra(LandingPageActivity.MESSAGE), Toast.LENGTH_LONG).show();
                downloadExpenses();
            }
        }
    }
}
