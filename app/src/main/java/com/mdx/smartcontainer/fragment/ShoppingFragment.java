package com.mdx.smartcontainer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.activity.ContainerActivity;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.MyDialogBuilders;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.ContainerModel;
import com.mdx.smartcontainer.model.NotificationModel;
import com.mdx.smartcontainer.model.ShopItem;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingFragment extends Fragment implements View.OnClickListener{

    private View rootView;
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<ShopItem> myList = new ArrayList<>();
    private MyAdapter mAdapter;

    //for error
    private LinearLayout container;
    private LinearLayout error_image;
    private Button try_again_button;
    private ImageView imageError;
    private TextView textError;

    public static ShoppingFragment newInstance() {
        ShoppingFragment fragment = new ShoppingFragment();
        return fragment;
    }

    public ShoppingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView  = inflater.inflate(R.layout.fragment_shopping, container, false);
        initializeView();
        return  rootView;
    }

    private void initializeView(){
        myList.clear();
        sessionManager = new SessionManager(getContext());
        indeterminate_progress =rootView.findViewById(R.id.navigation);
        mRecyclerView = rootView.findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);


        //for errors
        indeterminate_progress = rootView.findViewById(R.id.indeterminate_progress);
        error_image = rootView.findViewById(R.id.error_image);
        container = rootView.findViewById(R.id.container);
        imageError = rootView.findViewById(R.id.imageError);
        textError = rootView.findViewById(R.id.textError);
        try_again_button = rootView.findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(this);
        getShopping();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }else if (id == R.id.addContainer){

        }
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private List<ShopItem> dataList;
        private ShopItem dataModel;

        public MyAdapter(List<ShopItem> mList) {
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
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_shop, viewGroup, false);
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
                Picasso.with(getContext()).load(dataModel.getImage())
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(itemViewHolder.item_image);
                itemViewHolder.isbought.setChecked(dataModel.isIs_bought());
                itemViewHolder.item_name.setText(dataModel.getTitle());
                itemViewHolder.percent.setText(dataModel.getPercent_remaining()+"%");
                itemViewHolder.remaining.setText("Remaining : "+dataModel.getWeight_level_remaining());
                itemViewHolder.isbought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setBought(dataModel.getContainer_id(), isChecked, dataModel);
                    }
                });
            }
            else if (holder instanceof HomeFragment.MyAdapter.FooterViewHolder) {
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
            public ImageView item_image;
            public TextView item_name,percent,remaining;
            public View view;
            private CheckBox isbought;
            public ItemViewHolder(View v) {
                super(v);
                item_image= v.findViewById(R.id.item_image);
                item_name= v.findViewById(R.id.item_name);
                remaining= v.findViewById(R.id.remaining);
                isbought= v.findViewById(R.id.isbought);
                percent= v.findViewById(R.id.percent);
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

    private void getShopping(){
        indeterminate_progress.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.GET_SHOPPING_LIST,null, new Response.Listener<JSONObject>() {
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
                                    String container_id = jobj.getString("container_id");
                                    String image = jobj.getString("image");
                                    String title = jobj.getString("title");
                                    String weight_level_remaining = jobj.getString("weight_level_remaining");
                                    String percent_remaining = jobj.getString("percent_remaining");
                                    boolean is_bought = jobj.getBoolean("is_bought");
                                    ShopItem shopItem = new ShopItem(image,container_id,title,weight_level_remaining,percent_remaining,is_bought);
                                    myList.add(shopItem);
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

    private void setBought(String container_id, Boolean is_bought,ShopItem dataModel){
        JSONObject params = new JSONObject();
        try {
            params.put("is_bought", is_bought);
            params.put("container_id", container_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.SET_BOUGHT,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            dataModel.setIs_bought(is_bought);
                            mAdapter.notifyDataSetChanged();
                        }
                        else {
                            dataModel.setIs_bought(!is_bought);
                            mAdapter.notifyDataSetChanged();
                            MyDialogBuilders.displayPromptForError(getActivity(),response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(getActivity(),getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                MyDialogBuilders.displayPromptForError(getActivity(),"Error connecting "+error.getMessage());
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
}