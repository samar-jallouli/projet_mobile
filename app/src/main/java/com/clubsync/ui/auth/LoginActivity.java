package com.clubsync.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityLoginBinding;
import com.clubsync.ui.admin.AdminDashboardActivity;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.ui.member.MemberHomeActivity;
import com.clubsync.util.Constants;
import com.clubsync.util.Validators;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FirebaseRepository.getInstance();

        setupListeners();
        checkAutoLogin();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());
    }

    /**
     * Vérification de la session active (Auto-Login).
     */
    private void checkAutoLogin() {
        FirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser != null) {
            setLoading(true);
            fetchRoleAndRedirect(currentUser.getUid());
        }
    }

    /**
     * Traitement de la connexion avec validation des entrées.
     */
    private void performLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.err_empty_fields));
            return;
        }

        if (!Validators.isValidEmail(email)) {
            showError(getString(R.string.err_invalid_email));
            return;
        }

        if (!Validators.isValidPassword(password)) {
            showError(getString(R.string.err_short_password));
            return;
        }

        setLoading(true);

        repository.authenticate(email, password, new Callback<String>() {
            @Override
            public void onSuccess(String uid) {
                fetchRoleAndRedirect(uid);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError(getString(R.string.err_auth_failed) + "\n(" + error + ")");
            }
        });
    }

    /**
     * Récupère le rôle de l'utilisateur dans Firestore et effectue la redirection.
     */
    private void fetchRoleAndRedirect(String uid) {
        repository.getUserRole(uid, new Callback<String>() {
            @Override
            public void onSuccess(String role) {
                setLoading(false);
                if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                    redirectToDashboard(AdminDashboardActivity.class);
                } else if (Constants.ROLE_MEMBER.equalsIgnoreCase(role)) {
                    redirectToDashboard(MemberHomeActivity.class);
                } else {
                    repository.logout();
                    showError("Rôle non reconnu : " + role);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                repository.logout();
                showError(getString(R.string.err_role_not_found) + " (" + error + ")");
            }
        });
    }

    /**
     * Redirige vers l'activité cible en vidant la pile d'activités.
     */
    private void redirectToDashboard(Class<?> targetActivity) {
        Intent intent = new Intent(LoginActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Affiche ou masque la barre de progression.
     */
    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }
}
