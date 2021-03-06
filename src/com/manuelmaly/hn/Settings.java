package com.manuelmaly.hn;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    
    public static final String PREF_FONTSIZE = "pref_fontsize";
    public static final String pref_COLOR = "pref_background_color";    // Ramesh kumar coding part for change background color using radio button
    public static final String PREF_HTMLPROVIDER = "pref_htmlprovider";
    public static final String PREF_HTMLVIEWER = "pref_htmlviewer";
    public static final String PREF_USER = "pref_user";
    public static final String PREF_CONTENT = "pref_htmlcontent"; //------------- kevin's codes. about preference for change mode of web content ----------------
    
    public static final String USER_DATA_SEPARATOR = ":";
    
    public static String getFontSize(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_FONTSIZE, c.getString(R.string.pref_default_fontsize));
    }
    
    // Ramesh kumar coding part for change background color using radio button
    
    public static String getColor(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(pref_COLOR, c.getString(R.string.pref_default_background_color));
        
    }

    public static String getHtmlProvider(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_HTMLPROVIDER, c.getString(R.string.pref_default_htmlprovider));
    }

    public static String getHtmlViewer(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_HTMLVIEWER, c.getString(R.string.pref_default_htmlviewer));
    }
    
    //------------- kevin's codes. about preference for change mode of web content ----------------
    public static String getHtmlContent(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString(PREF_CONTENT, c.getString(R.string.pref_default_htmlcontent));
    }

    public static boolean isUserLoggedIn(Context c) {
        return !getUserName(c).equals("");
        
    }
       
    public static String getUserName(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        String[] userData = sharedPref.getString(PREF_USER, "").split(USER_DATA_SEPARATOR);
        if (userData.length > 0)
            return userData[0];
        return null;
    }
    
    public static String getUserToken(Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        String[] userData = sharedPref.getString(PREF_USER, "").split(USER_DATA_SEPARATOR);
        if (userData.length > 1)
            return userData[1];
        return null;
    }

    public static void setUserData(String userName, String userToken, Context c) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().putString(PREF_USER, userName + USER_DATA_SEPARATOR + userToken).commit();
    }

}
