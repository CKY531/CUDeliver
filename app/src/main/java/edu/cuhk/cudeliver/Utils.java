package edu.cuhk.cudeliver;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Utils {

    public static final int MESSAGE = 0;
    public static final int WARNING = 1;
    public static final int CAUTION = 2;

    public static void inputValidate(String email,String password1, String password2) throws Exception {
        if (email.length() == 0) {
            throw new Exception( "Email should not be empty");
        }
        if (password1.length() < 8) {
            throw new Exception("Password too short");
        }
        if (!password1.equals(password2))
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
            default:
        }
        s.show();
    }
}
