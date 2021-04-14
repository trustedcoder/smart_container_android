package com.mdx.smartcontainer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.MealModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestMealActivity extends AppCompatActivity  implements View.OnClickListener{
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<MealModel> myList = new ArrayList<>();
    private MyAdapter mAdapter;
    private Toolbar toolbar;

    //For the adapter use
    private Boolean loading = true;
    private Boolean isLastPage = false;
    private int pageCount=0;
    private ImageView imageError;
    private TextView textError;

    //for error
    private LinearLayout container;
    private LinearLayout error_image;
    private Button try_again_button;
    public static String people= "0";
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_meal);
        toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Meal Suggestion");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        extras=getIntent().getExtras();
        if (extras==null){
            finish();
        }
        else {
            people=extras.getString("people");
        }
        initializeView();
    }
    private void initializeView(){
        sessionManager = new SessionManager(getApplicationContext());
        indeterminate_progress =findViewById(R.id.navigation);
        mRecyclerView = findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(SuggestMealActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);


        //for errors
        imageError = findViewById(R.id.imageError);
        textError = findViewById(R.id.textError);
        indeterminate_progress = findViewById(R.id.indeterminate_progress);
        error_image = findViewById(R.id.error_image);
        container = findViewById(R.id.container);
        try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(this);
        getMeals();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){

        }
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private List<MealModel> dataList;
        private MealModel dataModel;

        public MyAdapter(List<MealModel> mList) {
            this.dataList = mList;
        }

        @Override
        public int getItemViewType(int position) {
            if(isPositionFooter(position)){
                return TYPE_FOOTER;
            }

            return TYPE_ITEM;
        }
        private boolean isPositionFooter(int position) {
            return position > dataList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_meal, viewGroup, false);
                return new ItemViewHolder(view);

            }
            else if (viewType == TYPE_FOOTER) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loading_linear,
                        viewGroup, false);
                return new FooterViewHolder(view);

            }

            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                dataModel = dataList.get(position);
                itemViewHolder.cookTime.setText("Cook time: "+dataModel.getCook_time());
                itemViewHolder.meal_name.setText(dataModel.getName());
                Picasso.with(getApplicationContext()).load(AppConfig.HOST+dataModel.getImage())
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(itemViewHolder.meal_image);

            }
            else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                footerViewHolder.progressBar.setIndeterminate(true);
                footerViewHolder.progressBar.setVisibility(View.GONE);
            }


        }


        @Override
        public int getItemCount() {
            return this.dataList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder{
            public ImageView meal_image;
            public TextView meal_name,cookTime;
            public View view;
            public ItemViewHolder(View v) {
                super(v);
                meal_image = v.findViewById(R.id.meal_image);
                meal_name = v.findViewById(R.id.meal_name);
                cookTime = v.findViewById(R.id.cookTime);
                view = v;
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            public View View;
            public ProgressBar progressBar;
            public FooterViewHolder(View v) {
                super(v);
                View = v;
                progressBar = itemView.findViewById(R.id.indeterminate_progress);
            }

        }


    }

    private void getMeals(){
        indeterminate_progress.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.SUGGEST_MEAL_LIST+"?people_count="+people,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                indeterminate_progress.setVisibility(View.GONE);
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            JSONArray ja = response.getJSONArray("data");
                            if (ja.length() > 0){
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jobj = ja.getJSONObject(i);
                                    String meal_id = jobj.getString("meal_id");
                                    String image = jobj.getString("image");
                                    String name = jobj.getString("name");
                                    String cook_time = jobj.getString("cook_time");
                                    MealModel mealModel = new MealModel(meal_id,image,name,cook_time);
                                    myList.add(mealModel);
                                }
                                mAdapter.notifyDataSetChanged();
                                loadingSuccess();
                            }
                            else {
                                loadingFailed(response.getString("message"),true);
                            }
                        }
                        else {
                            loadingFailed(response.getString("message"),false);
                        }
                    }
                    else {
                        loadingFailed("Server error",false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                indeterminate_progress.setVisibility(View.GONE);
                loadingFailed("Server error",false);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("authorization", sessionManager.getAuth());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void loadingFailed(String message, Boolean is_list_empty){
        container.setVisibility(View.GONE);
        indeterminate_progress.setVisibility(View.GONE);
        error_image.setVisibility(View.VISIBLE);
        textError.setText(message);
        if(is_list_empty){
            imageError.setVisibility(View.GONE);
            try_again_button.setVisibility(View.GONE);
        }
        else {
            imageError.setVisibility(View.VISIBLE);
            try_again_button.setVisibility(View.VISIBLE);
        }
    }

    private void loadingSuccess(){
        container.setVisibility(View.VISIBLE);
        indeterminate_progress.setVisibility(View.GONE);
        error_image.setVisibility(View.GONE);
    }
}