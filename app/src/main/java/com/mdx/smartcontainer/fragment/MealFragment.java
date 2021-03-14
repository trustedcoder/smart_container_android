package com.mdx.smartcontainer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.MealModel;
import com.mdx.smartcontainer.model.ShopItem;

import java.util.ArrayList;
import java.util.List;

public class MealFragment extends Fragment implements View.OnClickListener{

    private View rootView;
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<MealModel> myList = new ArrayList<>();
    private MyAdapter mAdapter;

    //For the adapter use
    private Boolean loading = true;
    private Boolean isLastPage = false;
    private int pageCount=0;

    //for error
    private LinearLayout container;
    private LinearLayout error_image;
    private Button try_again_button;

    public static MealFragment newInstance() {
        MealFragment fragment = new MealFragment();
        return fragment;
    }

    public MealFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView  = inflater.inflate(R.layout.fragment_meal, container, false);
        initializeView();
        return  rootView;
    }

    private void initializeView(){
        indeterminate_progress =rootView.findViewById(R.id.navigation);
        mRecyclerView = rootView.findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);

        myList.add(new MealModel());
        myList.add(new MealModel());
//        myList.add(new ContainerModel());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = mLayoutManager.findLastVisibleItemPosition();

                if (lastvisibleitemposition == mAdapter.getItemCount() - 1) {
                    if (!loading && !isLastPage) {
                        loading = true;
                        //loadMyInbox();
                    }
                }
            }
        });

        //for errors
        indeterminate_progress = rootView.findViewById(R.id.indeterminate_progress);
        error_image = rootView.findViewById(R.id.error_image);
        container = rootView.findViewById(R.id.container);
        try_again_button = rootView.findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(this);
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

            }
            else if (holder instanceof HomeFragment.MyAdapter.FooterViewHolder) {
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
            public TextView container_name,remaining,item,percent;
            public View view;
            public ItemViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                container_image= v.findViewById(R.id.container_image);
                container_name= v.findViewById(R.id.container_name);
                remaining= v.findViewById(R.id.remaining);
                item= v.findViewById(R.id.item);
                percent= v.findViewById(R.id.percent);
                view = v;
            }

            @Override
            public void onClick(View view) {
                dataModel = dataList.get(getAdapterPosition());

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
}