package com.example.employeemanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "EmployeeManagementSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMPLOYEE_ID = "employeeId";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(int accountId, String username, String role, int employeeId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_ACCOUNT_ID, accountId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putInt(KEY_EMPLOYEE_ID, employeeId);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getAccountId() {
        return prefs.getInt(KEY_ACCOUNT_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public int getEmployeeId() {
        return prefs.getInt(KEY_EMPLOYEE_ID, -1);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
