package com.clubsync.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityAddMemberBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.util.Constants;
import com.clubsync.util.Validators;
import com.google.firebase.Timestamp;

import java.util.UUID;

public class AddMemberActivity extends BaseActivity {

    private ActivityAddMemberBinding binding;
    private final String[] departments = {Constants.DEPT_TECH, Constants.DEPT_COM, Constants.DEPT_EVENT, Constants.DEPT_RH};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_members);
        setupSpinner();

        binding.btnSave.setOnClickListener(v -> saveMember());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, departments);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerDepartment.setAdapter(adapter);
    }

    private void saveMember() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String role = binding.etRole.getText() != null ? binding.etRole.getText().toString().trim() : "";
        String department = departments[binding.spinnerDepartment.getSelectedItemPosition()];

        boolean hasError = false;

        if (!Validators.isValidName(fullName)) {
            binding.etFullName.setError("Nom invalide (minimum 3 caractères)");
            hasError = true;
        }

        if (!Validators.isValidEmail(email)) {
            binding.etEmail.setError("Email invalide");
            hasError = true;
        }

        if (!Validators.isValidPhone(phone)) {
            binding.etPhone.setError("Numéro tunisien invalide (8 chiffres)");
            hasError = true;
        }

        if (role.isEmpty()) {
            binding.etRole.setError("Veuillez renseigner un rôle");
            hasError = true;
        }

        if (hasError) {
            showError("Veuillez corriger les champs indiqués");
            return;
        }

        setLoading(true);

        Member member = new Member();
        member.setFullName(fullName);
        member.setEmail(email);
        member.setPhone(phone);
        member.setRole(role);
        member.setDepartment(department);
        member.setStatus(Constants.STATUS_PENDING);
        member.setQrToken(UUID.randomUUID().toString());
        member.setJoinedAt(Timestamp.now());

        FirebaseRepository.getInstance().addMember(member, new Callback<String>() {
            @Override
            public void onSuccess(String memberId) {
                setLoading(false);
                Toast.makeText(AddMemberActivity.this, "Membre enregistré avec succès", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Erreur d'enregistrement : " + error);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!isLoading);
        binding.etFullName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPhone.setEnabled(!isLoading);
        binding.etRole.setEnabled(!isLoading);
        binding.spinnerDepartment.setEnabled(!isLoading);
    }
}
