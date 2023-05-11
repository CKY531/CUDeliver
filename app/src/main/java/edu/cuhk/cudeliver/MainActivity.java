package edu.cuhk.cudeliver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    Button loginBtn;
    TextView signup;
    TextView forget;
    View contentView;

    ProgressDialog progressDialog;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = findViewById(android.R.id.content);
//         init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://cudeliver-523c3-default-rtdb.asia-southeast1.firebasedatabase.app");
        users = db.getReference("Users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // init view
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signup = findViewById(R.id.text_signup);
        forget = findViewById(R.id.text_forget_password);
        loginBtn = findViewById(R.id.button_login);

        loginBtn.setOnClickListener(view -> {
            //hide keyboard
            Utils.hideKeyboard(view,MainActivity.this);
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            try {
                progressDialog.show();
                Utils.inputValidate(e,p);
                auth.signInWithEmailAndPassword(e,p).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressDialog.hide();
//                        Utils.showMessage(contentView,"Start activity here: not implemented",Utils.MESSAGE);
                        Intent intent = new Intent(MainActivity.this, OrderActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finishAffinity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        if (e.getMessage().equals("The password is invalid or the user does not have a password.")) {
                            Utils.showMessage(contentView,"Password incorrect",Utils.WARNING);
                        }else if (e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                            Utils.showMessage(contentView,"User not found.",Utils.WARNING);
                        }else
                        {
                            Log.d("AUTH",e.getMessage());
                            Utils.showMessage(contentView,"Unknown error.",Utils.WARNING);
                        }
                    }
                });
            }catch (Exception err){
                progressDialog.hide();
                String message = err.getMessage();
                Utils.showMessage(contentView,message,Utils.WARNING);
            }
        });

        signup.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
//                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        });

        forget.setOnClickListener(view -> {
            showForgetPasswordDialog();
        });
    }

    private void showForgetPasswordDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View forgetPasswordLayout = inflater.inflate(R.layout.dialog_forget_password,null);
        dialog.setView(forgetPasswordLayout);

        EditText email = forgetPasswordLayout.findViewById(R.id.text_forget_password_email);
        Button submit = forgetPasswordLayout.findViewById(R.id.button_forget_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide keyboard
                Utils.hideKeyboard(view,MainActivity.this);
                progressDialog.show();
                if (email.getText().toString().trim().length() == 0){
                    Utils.showMessage(contentView,"Please input email",Utils.MESSAGE);
                    return;
                }
                auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.hide();
                        Utils.showMessage(contentView,"Reset link sent, please check your email",Utils.MESSAGE);
                        Log.d("EMAIL","SENT");
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        if(e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted."))
                        {
                            Utils.showMessage(contentView,"This email has not registered",Utils.WARNING);
                        }else if(e.getMessage().equals("The email address is badly formatted."))
                        {
                            Utils.showMessage(contentView,"Email invalid",Utils.WARNING);
                        }
                        else{
                            Utils.showMessage(contentView,"Unknown error, try again later",Utils.WARNING);
                            Log.d("EMAIL",e.getMessage());
                        }
                    }
                });
            }
        });
        dialog.show();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        progressDialog.dismiss();
    }

    @Override
    public void onStart(){
        super.onStart();
        if (auth.getCurrentUser()!=null) {
            startActivity(new Intent(MainActivity.this, OrderActivity.class));
            finishAffinity();
        }
    }
}