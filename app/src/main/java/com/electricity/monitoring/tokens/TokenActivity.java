package com.electricity.monitoring.tokens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.electricity.monitoring.R;
import com.electricity.monitoring.adapter.NeighbourhoodAdapter;
import com.electricity.monitoring.adapter.TokenAdapter;
import com.electricity.monitoring.api.ApiClient;
import com.electricity.monitoring.api.ApiInterface;
import com.electricity.monitoring.auth.HomeRegistrationActivity;
import com.electricity.monitoring.database.DBHandler;
import com.electricity.monitoring.model.Neighbourhood;
import com.electricity.monitoring.model.Token;
import com.electricity.monitoring.utils.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenActivity extends AppCompatActivity {

    SharedPreferences sp;
    SwipeRefreshLayout mSwipeRefreshLayout;
    EditText etxtSearch;
    ProgressDialog loading;
    private RecyclerView recyclerView;
    TokenAdapter tokenAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    Utils utils;
    DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        recyclerView = findViewById(R.id.neighbourhood_recyclerview);
        etxtSearch = findViewById(R.id.etxt_search2);

        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_gradient));
        getSupportActionBar().setTitle("Purchased Tokens");

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        //set color of swipe refresh
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView
        recyclerView.setHasFixedSize(true);


        dbHandler = new DBHandler(TokenActivity.this);

        String email = dbHandler.loggedInUser();
        tokens(email);
    }



    public void tokens(String email){

        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.please_wait));
        loading.show();

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<ArrayList<Token>> call = apiInterface.getTokens(email);
        call.enqueue(new Callback<ArrayList<Token>>() {
            @Override
            public void onResponse(Call<ArrayList<Token>> call, Response<ArrayList<Token>> response) {
                loading.dismiss();
                if (response.isSuccessful()) {

                    ArrayList<Token> tokenArrayList;
                    tokenArrayList = response.body();

                    Toasty.success(TokenActivity.this, "Got data " + tokenArrayList.size(), Toast.LENGTH_SHORT).show();

                    if (tokenArrayList.isEmpty()) {

                        recyclerView.setVisibility(View.GONE);
                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);

                    } else {
                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);

                        recyclerView.setVisibility(View.VISIBLE);
                        tokenAdapter = new TokenAdapter(tokenArrayList, TokenActivity.this);
                        recyclerView.setAdapter(tokenAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Token>> call, Throwable t) {
                loading.dismiss();
                Toasty.error(TokenActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}