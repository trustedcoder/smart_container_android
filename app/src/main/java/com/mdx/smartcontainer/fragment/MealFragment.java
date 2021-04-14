package com.mdx.smartcontainer.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.clans.fab.FloatingActionButton;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.activity.ContainerActivity;
import com.mdx.smartcontainer.activity.SuggestMealActivity;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.MyDialogBuilders;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.ContainerModel;
import com.mdx.smartcontainer.model.IngredientModel;
import com.mdx.smartcontainer.model.MealModel;
import com.mdx.smartcontainer.model.ShopItem;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static android.app.Activity.RESULT_OK;

public class MealFragment extends Fragment implements View.OnClickListener{

    private View rootView;
    private ProgressBar indeterminate_progress;
    private SessionManager sessionManager;
    private RecyclerView mRecyclerView;
    private List<MealModel> myList = new ArrayList<>();
    private MyAdapter mAdapter;
    private MyAdapterA mAdapterA;
    private List<IngredientModel> myListA = new ArrayList<>();

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

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialogAddMeal,alertDialogSuggestMeal;
    private View addMealView,suggestMealView;
    LayoutInflater inflaterd;
    private FloatingActionButton add,suggest;
    private ProgressDialog progressDialog;

    private int PICK_IMAGE_ITEM = 1;
    private int PICK_IMAGE_ITEM_CROP = 2;
    private int MY_PERMISSIONS_REQUEST_STORAGE=100;
    private File file1;
    private String content_type1;
    private Bitmap bitmap_image;
    private ImageView selectImage;

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
        myList.clear();
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        sessionManager = new SessionManager(getContext());
        inflaterd = getLayoutInflater();
        add = rootView.findViewById(R.id.add);
        add.setOnClickListener(this);
        suggest = rootView.findViewById(R.id.suggest);
        suggest.setOnClickListener(this);
        indeterminate_progress =rootView.findViewById(R.id.navigation);
        mRecyclerView = rootView.findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(myList);
        mRecyclerView.setAdapter(mAdapter);



        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        //for errors
        indeterminate_progress = rootView.findViewById(R.id.indeterminate_progress);
        error_image = rootView.findViewById(R.id.error_image);
        container = rootView.findViewById(R.id.container);
        try_again_button = rootView.findViewById(R.id.try_again_button);
        try_again_button.setOnClickListener(this);
        imageError = rootView.findViewById(R.id.imageError);
        textError = rootView.findViewById(R.id.textError);
        getMeals();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }else if (id == R.id.add){
            setUpAddMealDialog("0","","","");
        }
        else if (id == R.id.suggest){
            setUpSuggestDialog();
        }
    }

    private void requestPermission(final String permission,final String permission2, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission2)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.permission_title));
            builder.setMessage(rationale);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissions(new String[]{permission,permission2, Manifest.permission.CAMERA}, requestCode);
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
                    bitmap_image= MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),result.getUri());
                    selectImage.setImageBitmap(bitmap_image);
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

    private void setUpAddMealDialog(String meal_id, String image, String name, String cook_time){
        addMealView = inflaterd.inflate(R.layout.dialog_add_meal, null);
        selectImage = addMealView.findViewById(R.id.selectImage);
        myListA.clear();
        RelativeLayout addImage = addMealView.findViewById(R.id.addImage);
        EditText meal_name = addMealView.findViewById(R.id.meal_name);
        EditText cookTime = addMealView.findViewById(R.id.cookTime);
        RecyclerView recycleView = addMealView.findViewById(R.id.recycleView);
        TextView errorText = addMealView.findViewById(R.id.errorText);
        ProgressBar progressBar = addMealView.findViewById(R.id.progress_bar);

        Picasso.with(getContext()).load(AppConfig.HOST+image)
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(selectImage);
        meal_name.setText(name);
        cookTime.setText(cook_time);

        recycleView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recycleView.setLayoutManager(mLayoutManager);
        mAdapterA = new MyAdapterA();
        recycleView.setAdapter(mAdapterA);

        getIngredients(meal_id,progressBar,errorText);

        Button btnContinue = addMealView.findViewById(R.id.btnContinue);
        if (!meal_id.equals("0")){
            btnContinue.setVisibility(View.GONE);
        }

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_ITEM);
//                  startActivityForResult(CropImage.getPickImageChooserIntent(getContext()),PICK_IMAGE_ITEM);
                }
                else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_storage_access), MY_PERMISSIONS_REQUEST_STORAGE);
                }
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = meal_name.getText().toString().trim();
                String cook_time = cookTime.getText().toString().trim();
                if(file1 == null){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Select an image");
                }
                else if (name.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Enter meal name");
                }
                else if (cook_time.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Enter cook time");
                }
                else {
                    addNewMeal(name,cook_time);
                }
            }
        });


        //Set up the Dialog
        alertDialogBuilder.setView(addMealView);
        alertDialogAddMeal = alertDialogBuilder.create();
        alertDialogAddMeal.setCancelable(true);
        alertDialogAddMeal.show();
    }

    private void setUpSuggestDialog(){
        suggestMealView = inflaterd.inflate(R.layout.dialog_suggest, null);
        EditText numberPeople = suggestMealView.findViewById(R.id.numberPeople);
        Button btnContinue = suggestMealView.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String people = numberPeople.getText().toString().trim();
                if(people.isEmpty()){
                    MyDialogBuilders.displayPromptForError(getActivity(),"Enter number of people to be served.");
                }
                else {
                    Intent intent = new Intent(getContext(), SuggestMealActivity.class);
                    intent.putExtra("people", people);
                    startActivity(intent);
                }
            }
        });


        //Set up the Dialog
        alertDialogBuilder.setView(suggestMealView);
        alertDialogSuggestMeal = alertDialogBuilder.create();
        alertDialogSuggestMeal.setCancelable(true);
        alertDialogSuggestMeal.show();
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
                Picasso.with(getContext()).load(AppConfig.HOST+dataModel.getImage())
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(itemViewHolder.meal_image);
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

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public ImageView meal_image;
            public TextView meal_name,cookTime;
            public View view;
            public ItemViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                meal_image = v.findViewById(R.id.meal_image);
                meal_name = v.findViewById(R.id.meal_name);
                cookTime = v.findViewById(R.id.cookTime);
                view = v;
            }

            @Override
            public void onClick(View view) {
                dataModel = dataList.get(getAdapterPosition());
                setUpAddMealDialog(dataModel.getMeal_id(), dataModel.getImage(), dataModel.getName(), dataModel.getCook_time());
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

    public class MyAdapterA extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private IngredientModel dataModel;

        public MyAdapterA() {
        }

        @Override
        public int getItemViewType(int position) {
            if(isPositionFooter(position)){
                return TYPE_FOOTER;
            }

            return TYPE_ITEM;
        }
        private boolean isPositionFooter(int position) {
            return position > myListA.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items_ingredient, viewGroup, false);
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
                dataModel = myListA.get(position);
                Picasso.with(getContext()).load(dataModel.getImage())
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(itemViewHolder.meal_image);
                itemViewHolder.meal_name.setText(dataModel.getName());
                itemViewHolder.unitText.setText(dataModel.getUnit());
                itemViewHolder.isAdded.setChecked(dataModel.isChecked());
                itemViewHolder.editText.setText(String.valueOf(dataModel.getQuantity()));
                itemViewHolder.isAdded.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        dataModel.setChecked(isChecked);
                    }
                });
                itemViewHolder.editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        double number = Double.parseDouble(s.toString());
                        dataModel.setQuantity(number);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

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
            return myListA.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            public ImageView meal_image;
            public TextView meal_name,unitText;
            public EditText editText;
            public CheckBox isAdded;
            public View view;
            public ItemViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);
                meal_image= v.findViewById(R.id.meal_image);
                meal_name= v.findViewById(R.id.meal_name);
                isAdded= v.findViewById(R.id.isAdded);
                unitText= v.findViewById(R.id.unitText);
                editText= v.findViewById(R.id.editText);
                view = v;
            }

            @Override
            public void onClick(View view) {
                dataModel = myListA.get(getAdapterPosition());

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

    private void getIngredients(String meal_id, ProgressBar indeterminate_progress, TextView errorText){
        indeterminate_progress.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.GET_ALL_INGREDIENT+"?meal_id="+meal_id,null, new Response.Listener<JSONObject>() {
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
                                    String container_id = jobj.getString("container_id");
                                    String image = jobj.getString("image");
                                    String unit = jobj.getString("unit");
                                    String name = jobj.getString("name");
                                    boolean is_added = jobj.getBoolean("is_added");
                                    double quantity = Double.parseDouble(jobj.getString("quantity"));
                                    IngredientModel ingredientModel = new IngredientModel(image,name,container_id,quantity, is_added,unit);
                                    myListA.add(ingredientModel);

                                }
                                mAdapterA.notifyDataSetChanged();
                                errorText.setVisibility(View.GONE);
                            }
                            else {
                                errorText.setVisibility(View.VISIBLE);
                                errorText.setText(response.getString("message"));
                            }
                        }
                        else {
                            errorText.setVisibility(View.VISIBLE);
                            errorText.setText(response.getString("message"));
                        }
                    }
                    else {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Server error");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                indeterminate_progress.setVisibility(View.GONE);
                errorText.setVisibility(View.VISIBLE);
                errorText.setText("Server error");
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

    private void addNewMeal(String name, String cook_time){
        showDialog("Please wait...");

        OkHttpClient okHttpClient = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("image", file1.getName(), okhttp3.RequestBody.create(MediaType.parse(content_type1), file1));
        builder.addFormDataPart("name", name);
        builder.addFormDataPart("cook_time", cook_time);

        String ingredentsText = "";
        for(int i = 0; i< myListA.size(); i++){
            IngredientModel ingredientModel = myListA.get(i);
            if(ingredientModel.isChecked()){
                ingredentsText = ingredentsText+ingredientModel.getContainer_id()+"|"+ingredientModel.getQuantity()+"*";

            }
        }
        System.out.println(ingredentsText);
        ingredentsText= ingredentsText.substring(0, ingredentsText.length() - 1);
        System.out.println(ingredentsText);
        builder.addFormDataPart("ingredient", ingredentsText);

        RequestBody requestBody = builder.build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("authorization", sessionManager.getAuth())
                .url(AppConfig.NEW_MEAL_SAVE)
                .post(requestBody)
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
                                    alertDialogAddMeal.cancel();
                                    MyDialogBuilders.displayPromptForError(getActivity(),message);
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

    private void getMeals(){
        indeterminate_progress.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.GET_MEAL_LIST,null, new Response.Listener<JSONObject>() {
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