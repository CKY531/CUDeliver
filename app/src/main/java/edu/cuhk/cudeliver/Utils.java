package edu.cuhk.cudeliver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

public class Utils {

    public static final int MESSAGE = 0;
    public static final int WARNING = 1;
    public static final int CAUTION = 2;
    public static final int NEUTRAL = 3;


    public static void inputValidate(String email,String phone,String password1, String password2) throws Exception {
        if (email.length() == 0) {
            throw new Exception( "Email should not be empty");
        }

        if (phone.length() == 0)
        {
            throw new Exception("Phone number not be empty");
        }

        if (phone.length() != 8)
        {
            throw new Exception("Invalid HK phone number");
        }

        if (password1.length() < 8) {
            throw new Exception("Password too short");
        }
        if (!passwordConfirm(password1,password2))
        {
            throw new Exception( "Password does not match");
        }

    }

    public static void inputValidate(String email,String password1) throws Exception {
        if (email.length() == 0) {
            throw new Exception( "Please enter email");
        }
        if (password1.length() == 0) {
            throw new Exception("Please enter password");
        }
    }

    public static boolean passwordConfirm(String password1,String password2) {
        return (password1.equals(password2));
    }

    public static void showMessage(View contentView,String message,int type ) {
        Snackbar s = Snackbar.make(contentView, message, Snackbar.LENGTH_LONG);
        switch(type){
            case WARNING:
                s.setBackgroundTint(Color.rgb(240,128,128)); // pinky
                s.setTextColor(Color.YELLOW);
                break;
            case MESSAGE:
                s.setBackgroundTint(Color.rgb(0,250,154)); // light green
                s.setTextColor(Color.BLACK);
                break;
            case NEUTRAL:
                s.setBackgroundTint(Color.rgb(237,237,237));
                s.setDuration(1000);
                s.setTextColor(Color.BLACK);
                break;
            default:
        }
        s.show();
    }

    public static void hideKeyboard(View view, Context context){
        //hide keyboard
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

