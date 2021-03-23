package com.mdx.smartcontainer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
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
import com.github.clans.fab.FloatingActionButton;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.MyDialogBuilders;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.model.NotificationModel;
import com.squareup.picasso.Picasso;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.LegendModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import at.grabner.circleprogress.CircleProgressView;

public class ContainerActivity extends AppCompatActivity implements View.OnClickListener{
    private Toolbar toolbar;
    private TextView itemName;
    private ImageView image_name;
    private CircleProgressView percentFull;
    private TextView capacity_name;
    private TextView remaining_name;
    private TextView state_name;
    private TextView countable_name;
    private TextView quantity_name;
    private ValueLineChart chart1;
    private LinearLayout container;
    private SessionManager sessionManager;

    private FloatingActionButton refill,delete;

    public static String container_id= "0";
    private Bundle extras;
    private Boolean is_countable = true;

    //for error
    private ProgressBar indeterminate_progress;
    private LinearLayout error_image;
    private Button try_again_button;
    private ImageView imageError;
    private TextView textError;

    private ScheduledExecutorService foregroundScheduler = null;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialogAddContainerThree;
    private View threeView;
    LayoutInflater inflaterd;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Details");
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
            container_id=extras.getString("container_id");
        }
        initView();

    }

    private void initView(){
        sessionManager = new SessionManager(getApplicationContext());
        itemName = findViewById(R.id.itemName);
        image_name = findViewById(R.id.image_name);
        percentFull = findViewById(R.id.percentFull);
        capacity_name = findViewById(R.id.capacity_name);
        remaining_name = findViewById(R.id.remaining_name);
        state_name = findViewById(R.id.state_name);
        countable_name = findViewById(R.id.countable_name);
        quantity_name = findViewById(R.id.quantity_name);
        chart1 = findViewById(R.id.chart1);
        container = findViewById(R.id.container);

        delete = findViewById(R.id.delete);
        refill = findViewById(R.id.refill);
        refill.setOnClickListener(this);
        delete.setOnClickListener(this);
        alertDialogBuilder = new AlertDialog.Builder(ContainerActivity.this);
        progressDialog = new ProgressDialog(ContainerActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        inflaterd = getLayoutInflater();

        //for errors
        indeterminate_progress = findViewById(R.id.indeterminate_progress);
        error_image = findViewById(R.id.error_image);
        container = findViewById(R.id.container);
        try_again_button = findViewById(R.id.try_again_button);
        imageError = findViewById(R.id.imageError);
        textError = findViewById(R.id.textError);
        try_again_button.setOnClickListener(this);

        getContainer();
    }

    private void getContainer(){
        container.setVisibility(View.GONE);
        indeterminate_progress.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, AppConfig.VIEW_CONTAINER+"?container_id="+container_id,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                indeterminate_progress.setVisibility(View.GONE);
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            itemName.setText(response.getString("name_item"));
                            percentFull.setValue(Float.valueOf(response.getString("percentage")));
                            percentFull.setText(response.getString("percentage"));
                            capacity_name.setText(response.getString("capacity"));
                            remaining_name.setText(response.getString("remaining"));
                            state_name.setText(response.getString("state"));
                            countable_name.setText(response.getString("countable"));
                            if(response.getString("countable").equals("No")){
                                is_countable = false;
                            }
                            quantity_name.setText(response.getString("quantity"));
                            Picasso.with(getApplicationContext()).load(AppConfig.HOST+response.getString("image"))
                                    .placeholder(R.drawable.no_image)
                                    .error(R.drawable.no_image)
                                    .into(image_name);

                            getSupportActionBar().setTitle(response.getString("name_container"));
                            JSONArray ja = response.getJSONArray("data");
                            if (ja.length() > 0){
                                ValueLineSeries series = new ValueLineSeries();
                                series.setColor(0xFF56B7F1);
                                JSONObject jobjr = ja.getJSONObject(0);
                                float start = jobjr.getInt("value");
                                JSONObject jobjre = ja.getJSONObject(ja.length()-1);
                                float end = jobjre.getInt("value");
                                series.addPoint(new ValueLinePoint("", start));
                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject jobj = ja.getJSONObject(i);
                                    float value = jobj.getInt("value");
                                    String date_created = jobj.getString("date_created");
                                    series.addPoint(new ValueLinePoint(date_created, value));
                                }
                                series.addPoint(new ValueLinePoint("", end));
                                chart1.addSeries(series);
                                chart1.startAnimation();
                                loadingSuccess();
                            }
                            else {
                                loadingSuccess();
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

    private void deleteContainer(){
        showDialog("Deleting...");
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DELETE, AppConfig.DELETE_CONTAINER+"?container_id="+container_id,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            MyDialogBuilders.displayPromptForFinish(ContainerActivity.this,response.getString("message"));
                        }
                        else {
                            MyDialogBuilders.displayPromptForError(ContainerActivity.this,response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(ContainerActivity.this,"Server error");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(ContainerActivity.this,"Server error");
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

    public void startSchedule(final Activity activity, String container_id, TextView reading, TextView messageTxt){
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

        startSchedule(ContainerActivity.this,container_id,reading,itemMessage);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    calibrate(container_id);
                } catch (JSONException e) {
                    MyDialogBuilders.displayPromptForError(ContainerActivity.this,e.toString());
                }
            }
        });
        alertDialogAddContainerThree.show();
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
                        MyDialogBuilders.displayPromptForError(ContainerActivity.this,response.getString("message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(ContainerActivity.this,"Error connecting "+error.getMessage());
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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.try_again_button){
            loadingSuccess();
            getContainer();
        }
        if (id == R.id.delete){
            final AlertDialog.Builder builder =  new AlertDialog.Builder(ContainerActivity.this);

            builder.setMessage(Html.fromHtml("This action will also delete all meals ingredient, shopping list and notifications relating to this container.\n\nAre you sure you want to delete this container?\n\n"))
                    .setTitle("Delete container")
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            })
                    .setPositiveButton("Delete",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    deleteContainer();
                                }
                            })
                    .setCancelable(false);
            builder.create().show();
        }
        if (id == R.id.refill){
            setUpThreeDialog(is_countable);
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