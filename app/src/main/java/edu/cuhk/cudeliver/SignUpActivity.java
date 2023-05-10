package edu.cuhk.cudeliver;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.User;

public class SignUpActivity extends AppCompatActivity {

    EditText email;
    EditText phone;
    EditText password1;
    EditText password2;
    Button signupBtn;
    View contentView;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        contentView = findViewById(android.R.id.content);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://cudeliver-81db2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        users = db.getReference("Users");

        // init view
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        password1 = findViewById(R.id.password1);
        password2 = findViewById(R.id.password2);
        signupBtn = findViewById(R.id.button_signup);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Creating account...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        // signup onClick
        signupBtn.setOnClickListener(view -> {
            //hide keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            progressDialog.show();

            String e = email.getText().toString().trim();
            String tel = phone.getText().toString().trim();
            String p1 = password1.getText().toString().trim();
            String p2 = password2.getText().toString().trim();
            try {
                Utils.inputValidate(e,tel,p1,p2);
                auth.createUserWithEmailAndPassword(e,p1).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        if (e.getMessage().equals("The email address is already in use by another account.")) {
                            Utils.showMessage(contentView,"This email is already been registered",Utils.WARNING);
                        }else if (e.getMessage().equals("The email address is badly formatted.")) {
                            Utils.showMessage(contentView,"Please input a correct email.",Utils.WARNING);
                        }
                        else{
                            Utils.showMessage(contentView,"Unknown error.",Utils.WARNING);
                        }
                    }}).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String userId = auth.getCurrentUser().getUid();
                        User user = new User(e,tel);
                        users.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.hide();
                                Utils.showMessage(contentView,"Successfully signed up, going to login page...",Utils.MESSAGE);
                                //timeout
                                new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                finish();
                                            }
                                        }, 3000);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.hide();
                                auth.getCurrentUser().delete();
                                Utils.showMessage(contentView,"Unknown error, try again later",Utils.MESSAGE);
                            }
                        });

                    }
                });
            }catch (Exception err){
                progressDialog.hide();
                String message = err.getMessage();
                Utils.showMessage(contentView,message,Utils.WARNING);
            }
        });
    }

    @Override
    public void finish(){
        super.finish();
//        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}