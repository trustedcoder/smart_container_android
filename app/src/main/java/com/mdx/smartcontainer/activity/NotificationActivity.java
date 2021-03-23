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
import com.mdx.smartcontainer.model.ContainerModel;
import com.mdx.smartcontainer.model.NotificationModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener{
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<NotificationModel> myList = new ArrayList<>();
    private MyAdapter mAdapter;
    private Toolbar toolbar;

    //For the adapter use
    private Boolean loading = true;
    private Boolean isLastPage = false;
    private int pageCount=0;

    //for error
    private LinearLayout container;
    private LinearLayout error_image;
    private Button try_again_button;
    private ImageView imageError;
    private TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notification");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initializeView();
    }

    private void initializeView(){
        sessionManager = new SessionManager(getApplicationContext());

        pageCount = 0;
        myList.clear();
        mRecyclerView = findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(NotificationActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = mLayoutManager.findLastVisibleItemPosition();

                if (lastvisibleitemposition == mAdapter.getItemCount() - 1) {
                    if (!loading && !isLastPage) {
                        loading = true;
                        getNotifications();
                    }
                }
            }
        });

        //for errors
        indeterminate_progress = findViewById(R.id.indeterminate_progress);
        error_image = findViewById(R.id.error_image);
        container = findViewById(R.id.container);
        try_again_button = findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(this);
        imageError = findViewById(R.id.imageError);
        textError = findViewById(R.id.textError);
        getNotifications();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){
            loadingSuccess();
            pageCount = 0;
            myList.clear();
            getNotifications();
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private List<NotificationModel> dataList;
        private NotificationModel dataModel;

        public MyAdapter(List<NotificationModel> mList) {
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
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_notify, viewGroup, false);
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
                if(dataModel.getImage() == 1){
                    itemViewHolder.container_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_inventory_red));
                }
                else if (dataModel.getImage() == 3){
                    itemViewHolder.container_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_inventory_green));
                }
                else {
                    itemViewHolder.container_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_inventory_orange));
                }
                itemViewHolder.container_name.setText(dataModel.getTitle());
                itemViewHolder.remaining.setText(dataModel.getDate_ago());

            }
            else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                footerViewHolder.progressBar.setIndeterminate(true);
                if (isLastPage){
                    footerViewHolder.progressBar.setVisibility(View.GONE);
                }
                else {
                    footerViewHolder.progressBar.setVisibility(View.VISIBLE);
                }
            }


        }


        @Override
        public int getItemCount() {
            return this.dataList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public ImageView container_image;
            public TextView container_name,remaining;
            public View view;
            public ItemViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                container_image= v.findViewById(R.id.container_image);
                container_name= v.findViewById(R.id.container_name);
                remaining= v.findViewById(R.id.remaining);
                view = v;
            }

            @Override
            public void onClick(View view) {
                dataModel = dataList.get(getAdapterPosition());
                Intent intent = new Intent(getApplicationContext(), ContainerActivity.class);
                intent.putExtra("container_id", dataModel.getContainer_id());
                startActivity(intent);
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

    private void getNotifications(){
        if (pageCount == 0){
            indeterminate_progress.setVisibility(View.VISIBLE);
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.GET_NOTIFICATIONS+"?start="+pageCount,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                indeterminate_progress.setVisibility(View.GONE);
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            isLastPage = response.getBoolean("is_last_page");
                            pageCount = pageCount+20;
                            JSONArray ja = response.getJSONArray("data");
                            if (ja.length() > 0){
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jobj = ja.getJSONObject(i);
                                    String container_id = jobj.getString("container_id");
                                    int image = jobj.getInt("image");
                                    String title = jobj.getString("title");
                                    String date_ago = jobj.getString("date_ago");
                                    NotificationModel notificationModel = new NotificationModel(container_id, image, title, date_ago);
                                    myList.add(notificationModel);

                                }
                                mAdapter.notifyDataSetChanged();
                                loading = false;
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