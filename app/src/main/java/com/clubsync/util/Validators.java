package com.clubsync.util;

import android.util.Patterns;

public final class Validators {
    private Validators() {}

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 3;
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[\\s-]", "");
        // Numéro tunisien : 8 chiffres (commençant généralement par 2, 4, 5 ou 9)
        return cleaned.matches("^[2-9]\\d{7}$") || cleaned.matches("^\\d{8}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
