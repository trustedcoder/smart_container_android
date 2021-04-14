package com.mdx.smartcontainer.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.AppConfig;
import com.mdx.smartcontainer.app.AppController;
import com.mdx.smartcontainer.app.HelperClass;
import com.mdx.smartcontainer.app.MyDialogBuilders;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.others.Bungee;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button facebookButton,googleButton,emailButton,btnRegister,btnLogin,btnSubmit,btnSubmitsocial;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialogSignUp,alertDialogSignIn,alertDialogForgot,alertDialogsocial;
    private View signUpView,loginView,forgotView,preSignUpView;
    LayoutInflater inflater;
    private EditText nameEditTextSignUp,emailEditTextSignUp,
            passwordEditTextSignUp,emailEditTextSignIn,
            passwordEditTextSignIn,emailEditTextforgot,emailEditTextsocial,nameEditText;
    private LinearLayout signIn,signUp;
    private ProgressBar progress_bar;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 2;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        intializeGoogleSignIn();

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        String accessToken = loginResult.getAccessToken().getToken();
                        loginWithFB(accessToken);
                    }

                    @Override
                    public void onCancel() {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Facebook login cancelled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"error"+exception.getMessage());
                    }
                });
        initializeViews();
        setUpSignUpDialog();
        setUpSignInDialog();
    }

    private void initializeViews(){
        facebookButton = findViewById(R.id.facebookButton);
        googleButton = findViewById(R.id.googleButton);
        emailButton = findViewById(R.id.emailButton);
        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        emailButton.setOnClickListener(this);
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.GONE);
        inflater = getLayoutInflater();
        alertDialogBuilder = new AlertDialog.Builder(this);
        progressDialog = new ProgressDialog(WelcomeActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        sessionManager = new SessionManager(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        progress_bar.setVisibility(View.GONE);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                loginWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                System.out.println(e.toString());
            }

        }

    }

    @Override
    public void onStart(){
        super.onStart();
        Bungee.split(WelcomeActivity.this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.facebookButton){
            progress_bar.setVisibility(View.VISIBLE);
            LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, null);
        }
        else if (view.getId() == R.id.googleButton){
            progress_bar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else if (view.getId() == R.id.emailButton){
            alertDialogSignUp.show();
        }
    }

    private void intializeGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setUpSignUpDialog(){
        signUpView = inflater.inflate(R.layout.sign_up, null);
        nameEditTextSignUp = signUpView.findViewById(R.id.name);
        emailEditTextSignUp = signUpView.findViewById(R.id.email);
        passwordEditTextSignUp = signUpView.findViewById(R.id.password);
        signIn = signUpView.findViewById(R.id.signIn);
        btnRegister = signUpView.findViewById(R.id.btnRegister);

        //Set up the Dialog
        alertDialogBuilder.setView(signUpView);
        alertDialogSignUp = alertDialogBuilder.create();
        alertDialogSignUp.setCancelable(true);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=nameEditTextSignUp.getText().toString().trim();
                String password=passwordEditTextSignUp.getText().toString().trim();
                String email=emailEditTextSignUp.getText().toString().trim();
                if (name.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Name is empty!");
                }
                else if (email.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Email is empty!");
                }
                else if (password.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Password is empty!");
                }
                else if (!HelperClass.isValidEmail(email)){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Please enter a valid email address");
                }
                else if (!(password.length() >= 6)){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Password must be greater than 5 characters");
                }
                else {
                    alertDialogSignUp.cancel();
                    registerEmail(name,password,email);
                }

            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogSignUp.cancel();
                alertDialogSignIn.show();
            }
        });
    }

    private void setUpSignInDialog(){
        loginView = inflater.inflate(R.layout.sign_in, null);
        emailEditTextSignIn = loginView.findViewById(R.id.email);
        passwordEditTextSignIn = loginView.findViewById(R.id.password);
        btnLogin = loginView.findViewById(R.id.btnLogin);
        signUp = loginView.findViewById(R.id.signUp);

        //Set up the Dialog
        alertDialogBuilder.setView(loginView);
        alertDialogSignIn = alertDialogBuilder.create();
        alertDialogSignIn.setCancelable(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditTextSignIn.getText().toString().trim();
                String password = passwordEditTextSignIn.getText().toString().trim();
                if (email.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Email is empty!");
                }
                else if (password.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Password is empty!");
                }
                else {
                    alertDialogSignIn.cancel();
                    loginEmail(email,password);
                }
            }
        });


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogSignIn.cancel();
                alertDialogSignUp.show();
            }
        });
    }


    private void setUpPreRegisterDialog(final String name, String email){
        preSignUpView = inflater.inflate(R.layout.pre_sign_up, null);
        emailEditTextsocial = preSignUpView.findViewById(R.id.email);
        nameEditText = preSignUpView.findViewById(R.id.name);
        btnSubmitsocial = preSignUpView.findViewById(R.id.btnRegister);
        if (!email.isEmpty()){
            emailEditTextsocial.setEnabled(false);
        }
        emailEditTextsocial.setText(email);
        nameEditText.setText(name);


        //Set up the Dialog
        alertDialogBuilder.setView(preSignUpView);
        alertDialogsocial = alertDialogBuilder.create();
        alertDialogsocial.setCancelable(false);

        btnSubmitsocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditTextsocial.getText().toString().trim();
                String name = nameEditText.getText().toString().trim();
                if (email.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Email is empty!");
                }
                else if (!HelperClass.isValidEmail(email)){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Please enter a valid email address");
                }
                else if (name.isEmpty()){
                    MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Name is empty!");
                }
                else {
                    preRegister(email,name);
                }
            }
        });

        alertDialogsocial.show();

    }

    private void loginWithFB(final String access_token){
        LoginManager.getInstance().logOut();
        showDialog("Please wait...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", access_token);
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.FB_LOGIN,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                 hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            sessionManager.setAuth(response.getString("authorization"));
                            if (response.getBoolean("is_profiled")){
                                sessionManager.setLogin(true);
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                String email = response.getString("email");
                                String name = response.getString("name");
                                setUpPreRegisterDialog(name,email);
                            }
                        }
                        else {
                            MyDialogBuilders.displayPromptForError(WelcomeActivity.this,response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Error connecting ");
            }
        });

        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void loginWithGoogle(final String access_token){
        mGoogleSignInClient.signOut();
        showDialog("Please wait...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", access_token);
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.GOOGLE_LOGIN,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            sessionManager.setAuth(response.getString("authorization"));
                            if (response.getBoolean("is_profiled")){
                                sessionManager.setLogin(true);
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                String email = response.getString("email");
                                String name = response.getString("name");
                                setUpPreRegisterDialog(name,email);
                            }
                        }
                        else {
                            MyDialogBuilders.displayPromptForError(WelcomeActivity.this,response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Error connecting "+error.toString());
            }
        });

        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void preRegister(String email, String name){
        showDialog("Please wait...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("email", email);
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT,AppConfig.UPDATE_PROFILE,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            sessionManager.setLogin(true);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            MyDialogBuilders.displayPromptForError(WelcomeActivity.this,response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Error connecting "+error.getMessage());
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

    private void registerEmail(String name, String password, String email) {
        showDialog("Please wait...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("fullname", name);
        params.put("password", password);
        params.put("email", email);
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.EMAIL_REGISTER,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,response.getString("message"));
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Error connecting");
            }
        });

        AppController.getInstance().addToRequestQueue(jsonRequest);
    }

    private void loginEmail(String email, String password) {
        showDialog("Please wait...");
        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.EMAIL_LOGIN,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideDialog();
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            sessionManager.setLogin(true);
                            sessionManager.setAuth(response.getString("authorization"));
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            MyDialogBuilders.displayPromptForError(WelcomeActivity.this,response.getString("message"));
                        }
                    }
                    else {
                        MyDialogBuilders.displayPromptForError(WelcomeActivity.this,getResources().getString(R.string.accuracy));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                MyDialogBuilders.displayPromptForError(WelcomeActivity.this,"Error connecting");
            }
        });

        AppController.getInstance().addToRequestQueue(jsonRequest);
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


}
