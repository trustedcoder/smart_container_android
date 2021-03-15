package com.mdx.smartcontainer.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.activity.ContainerActivity;
import com.mdx.smartcontainer.activity.MainActivity;
import com.mdx.smartcontainer.activity.WelcomeActivity;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.HelperClass;
import com.mdx.smartcontainer.app.MyDialogBuilders;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.ContainerModel;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<ContainerModel> myList = new ArrayList<>();
    private MyAdapter mAdapter;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialogAddContainerOne,alertDialogAddContainerTwo,alertDialogAddContainerThree;
    private FloatingActionButton addContainer;
    private View oneView,twoView,threeView;
    LayoutInflater inflaterd;
    private int PICK_IMAGE_ITEM = 1;
    private int PICK_IMAGE_ITEM_CROP = 2;
    private int MY_PERMISSIONS_REQUEST_STORAGE=100;
    private File file1;
    private ProgressDialog progressDialog;
    private String content_type1;
    private String container_id_main;


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

    private ScheduledExecutorService foregroundScheduler = null;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        rootView  = inflater.inflate(R.layout.fragment_home, container, false);
        initializeView();
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_storage_access), MY_PERMISSIONS_REQUEST_STORAGE);

        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        setUpOneDialog();
        return  rootView;
    }

    private void initializeView(){
        sessionManager = new SessionManager(getContext());
        indeterminate_progress =rootView.findViewById(R.id.navigation);
        addContainer = rootView.findViewById(R.id.addContainer);
        addContainer.setOnClickListener(this);

        pageCount = 0;
        myList.clear();
        mRecyclerView = rootView.findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);
        getContainers();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = mLayoutManager.findLastVisibleItemPosition();

                if (lastvisibleitemposition == mAdapter.getItemCount() - 1) {
                    if (!loading && !isLastPage) {
                        loading = true;
                        getContainers();
                    }
                }
            }
        });

        //for errors
        indeterminate_progress = rootView.findViewById(R.id.indeterminate_progress);
        error_image = rootView.findViewById(R.id.error_image);
        container = rootView.findViewById(R.id.container);
        try_again_button = rootView.findViewById(R.id.try_again_button);
        imageError = rootView.findViewById(R.id.imageError);
        textError = rootView.findViewById(R.id.textError);
        try_again_button.setOnClickListener(this);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        inflaterd = getLayoutInflater();
    }

    private void setUpOneDialog(){
        oneView = inflaterd.inflate(R.layout.dialog_put_container1, null);
        Button btnContinue = oneView.findViewById(R.id.btnContinue);
        EditText containerId = oneView.findViewById(R.id.containerId);
        EditText nameContainer = oneView.findViewById(R.id.nameContainer);
        Spinner stateSpinner = oneView.findViewById(R.id.stateSpinner);
        Spinner isedibleSpinner = oneView.findViewById(R.id.isedible);

        //Set up the Dialog
        alertDialogBuilder.setView(oneView);
        alertDialogAddContainerOne = alertDialogBuilder.create();
        alertDialogAddContainerOne.setCancelable(true);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String container_id = containerId.getText().toString().trim();
                String name = nameContainer.getText().toString().trim();
                String state = stateSpinner.getSelectedItem().toString();
                String isedible = isedibleSpinner.getSelectedItem().toString();
                if (container_id.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Container ID is empty!");
                }
                else if (name.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Name is empty!");
                }
                else if (state.equals("Select State Of The Item")){
                    MyDialogBuilders.displayPromptForError(getActivity(),"State not selected!");
                }
                else if (isedible.equals("Is This Item Edible?")){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Is Edible not selected!");
                }
                else {
                    container_id_main = container_id;
                    alertDialogAddContainerOne.cancel();
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            addContainerOne(container_id,name,state,isedible);
                        } catch (JSONException e) {
                            MyDialogBuilders.displayPromptForError(getActivity(),e.toString());
                            e.printStackTrace();
                        }
                    }
                    else {
                        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_storage_access), MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                }

            }
        });
    }

    private void setUpTwoDialog(String name){
        twoView = inflaterd.inflate(R.layout.dialog_put_container2, null);
        Button btnContinue = twoView.findViewById(R.id.btnContinue);
        EditText itemName = twoView.findViewById(R.id.itemName);
        itemName.setText(name);
        Spinner IsCountable = twoView.findViewById(R.id.IsCountable);

        //Set up the Dialog
        alertDialogBuilder.setView(twoView);
        alertDialogAddContainerTwo = alertDialogBuilder.create();
        alertDialogAddContainerTwo.setCancelable(false);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = itemName.getText().toString().trim();
                String iscountable = IsCountable.getSelectedItem().toString();
                if (name.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Name is empty!");
                }
                else if (iscountable.equals("Is This Item Countable?")){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Is countable item not selected!");
                }
                else {
                    alertDialogAddContainerTwo.cancel();
                    try {
                        addContainerTwo(container_id_main,name, iscountable);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        alertDialogAddContainerTwo.show();
    }

    private void setUpThreeDialog(Boolean is_countable){
        threeView = inflaterd.inflate(R.layout.dialog_put_container3, null);
        Button btnContinue = threeView.findViewById(R.id.btnContinue);
        TextView itemMessage = threeView.findViewById(R.id.message);
        TextView reading = threeView.findViewById(R.id.reading);

        if(is_countable){
            itemMessage.setText("Place one of the item in the container");
        }
        else {
            itemMessage.setText("Place all item in the container");
        }

        //Set up the Dialog
        alertDialogBuilder.setView(threeView);
        alertDialogAddContainerThree = alertDialogBuilder.create();
        alertDialogAddContainerThree.setCancelable(false);

        startSchedule(getActivity(),container_id_main,reading,itemMessage);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    calibrate(container_id_main);
                } catch (JSONException e) {
                    MyDialogBuilders.displayPromptForError(getActivity(),e.toString());
                }
            }
        });
        alertDialogAddContainerThree.show();
    }

    private void requestPermission(final String permission,final String permission2, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission2)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.permission_title));
            builder.setMessage(rationale);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{permission,permission2,Manifest.permission.CAMERA}, requestCode);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        } else {
            requestPermissions(new String[]{permission,permission2}, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_ITEM_CROP) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                file1  = new File(imageUri.getPath());
                content_type1  = getMimeType(file1.getPath());
                try {
                    uploadImageDetect(container_id_main,file1,content_type1);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                MyDialogBuilders.displayPromptForError(getActivity(),"Cropping failed");
            }
        }
        if(requestCode == PICK_IMAGE_ITEM && resultCode == RESULT_OK){
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            if (CropImage.isReadExternalStoragePermissionsRequired(getActivity(), imageUri)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                startCropImageActivity(imageUri,PICK_IMAGE_ITEM_CROP,550,370,3000,3000);
            }
        }
    }

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void startCropImageActivity(Uri imageUri,int myResultCode,int minCropResultWidth,int minCropResultHeight,int maxCropResultWidth,int maxCropResultHeight) {
        Intent intents = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setMinCropResultSize(minCropResultWidth, minCropResultHeight)
                .setMaxCropResultSize(maxCropResultWidth,maxCropResultHeight)
                .getIntent(getContext());
        startActivityForResult(intents, myResultCode);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){
            assert getFragmentManager() != null;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }else if (id == R.id.addContainer){
            alertDialogAddContainerOne.show();
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private List<ContainerModel> dataList;
        private ContainerModel dataModel;

        public MyAdapter(List<ContainerModel> mList) {
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
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_container, viewGroup, false);
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
                itemViewHolder.container_name.setText(dataModel.getName_container());
                itemViewHolder.remaining.setText("Remaining : "+dataModel.getRemaining());
                itemViewHolder.percent.setText(dataModel.getPercentage()+"%");
                itemViewHolder.item.setText("Item : "+dataModel.getName_item());

                Picasso.with(getContext()).load(dataModel.getImage())
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(itemViewHolder.container_image);
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
                Intent intent = new Intent(getContext(), ContainerActivity.class);
                intent.putExtra("container_id",1);
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

    private void showDialog(String message) {
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void addContainerOne(String container_id, String name, String state, String is_edible) throws JSONException {
        showDialog("Please wait...");
        Boolean is_edible_v = false;
        if(is_edible.equals("Yes")){
            is_edible_v = true;
        }
        JSONObject params = new JSONObject();
        params.put("name", name);
        params.put("is_edible", is_edible_v);
        params.put("state", state);
        params.put("container_id", container_id);


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.ADD_CONTAINER_ONE,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_ITEM);
//                            startActivityForResult(CropImage.getPickImageChooserIntent(getContext()),PICK_IMAGE_ITEM);
                        }
                        else {
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
                hideDialog();
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

    private void uploadImageDetect(String container_id, File file,String content_type){
        showDialog("Detecting object...");
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), okhttp3.RequestBody.create(MediaType.parse(content_type), file))
                .addFormDataPart("container_id", container_id)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("authorization", sessionManager.getAuth())
                .url(AppConfig.DETECT_OBJECT)
                .patch(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                        MyDialogBuilders.displayPromptForError(getActivity(), "Request Failed");
                    }
                });


            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                    }
                });
                try {
                    //System.out.println(response.body().string());
                    JSONObject jObj = new JSONObject(response.body().string());

                    if (jObj.has("errors")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyDialogBuilders.displayPromptForError(getActivity(), "An error occurred");
                            }
                        });

                    } else {
                        int status = jObj.getInt("status");
                        final String message = jObj.getString("message");
                        if (status == 1) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        setUpTwoDialog(jObj.getString("name"));
                                    } catch (JSONException e) {
                                        MyDialogBuilders.displayPromptForError(getActivity(),e.toString());
                                    }
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MyDialogBuilders.displayPromptForError(getActivity(), message);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println(e.toString() +"Error HERE");
                }
            }
        });


    }

    private void addContainerTwo(String container_id, String name, String is_countable) throws JSONException {
        showDialog("Please wait...");
        Boolean is_countable_v = false;
        if(is_countable.equals("Yes")){
            is_countable_v = true;
        }
        JSONObject params = new JSONObject();
        params.put("name_item", name);
        params.put("is_countable", is_countable_v);
        params.put("container_id", container_id);


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.ADD_CONTAINER_TWO,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            setUpThreeDialog(response.getBoolean("is_countable"));
                        }
                        else {
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
                hideDialog();
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

    private void check_for_one(String container_id, TextView reading, TextView messageTxt){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.CHECK_FOR_ONE+"?container_id="+container_id,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            reading.setText(response.getString("current_reading"));
                            messageTxt.setText(response.getString("message"));

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
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

    public void startSchedule(final Activity activity,String container_id, TextView reading, TextView messageTxt){
        if(foregroundScheduler == null){
            foregroundScheduler = Executors.newSingleThreadScheduledExecutor();
            foregroundScheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            check_for_one(container_id, reading, messageTxt);
                        }
                    });
                }
            }, 5, 5, TimeUnit.SECONDS);
        }
    }

    public void stopSchedule(){
        if (!(foregroundScheduler == null)){
            foregroundScheduler.shutdownNow();
            foregroundScheduler = null;
        }
    }

    private void calibrate(String container_id) throws JSONException{
        showDialog("Calibrating ...");
        JSONObject params = new JSONObject();
        params.put("container_id", container_id);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.CALIBRATE,params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            alertDialogAddContainerThree.cancel();
                            stopSchedule();
                        }
                        MyDialogBuilders.displayPromptForError(getActivity(),response.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
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

    private void getContainers(){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.GET_CONTAINERS+"?start="+pageCount,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
                                    String name_item = jobj.getString("name_item");
                                    String remaining = jobj.getString("remaining");
                                    String name_container = jobj.getString("name_container");
                                    String image = jobj.getString("image");
                                    String percentage = jobj.getString("percentage");
                                    String public_id = jobj.getString("public_id");
                                    ContainerModel containerModel = new ContainerModel(name_item,remaining,name_container,percentage,public_id,image);
                                    myList.add(containerModel);

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