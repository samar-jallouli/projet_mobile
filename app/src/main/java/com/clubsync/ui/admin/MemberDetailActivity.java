package com.clubsync.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityMemberDetailBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.ui.common.MemberListActivity;
import com.clubsync.util.Constants;
import com.clubsync.util.QrUtils;
import com.clubsync.util.Validators;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MemberDetailActivity extends BaseActivity {

    private ActivityMemberDetailBinding binding;
    private Member currentMember;
    private final String[] statusOptions = {Constants.STATUS_ACTIVE, Constants.STATUS_PENDING};

    private static final int[] AVATAR_COLORS = {
            0xFF6B4EFF, 0xFF0D9488, 0xFFEA580C, 0xFFE11D48, 0xFF7C3AED, 0xFF0284C7, 0xFF059669
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupStatusSpinner();

        String memberId = getIntent().getStringExtra(MemberListActivity.EXTRA_MEMBER_ID);
        if (memberId == null || memberId.trim().isEmpty()) {
            showError("Identifiant du membre introuvable");
            finish();
            return;
        }

        loadMemberData(memberId);
        setupListeners();
    }

    private void setupStatusSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, statusOptions);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerDetailStatus.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSaveDetail.setOnClickListener(v -> saveMemberChanges());
        binding.btnDeleteMember.setOnClickListener(v -> confirmDeleteMember());
    }

    private void loadMemberData(String memberId) {
        setLoading(true);
        FirebaseRepository.getInstance().getMemberById(memberId, new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                setLoading(false);
                currentMember = member;
                populateViews();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Erreur : " + error);
                finish();
            }
        });
    }

    private void populateViews() {
        if (currentMember == null) return;

        // Résumé
        binding.tvSummaryName.setText(currentMember.getFullName() != null ? currentMember.getFullName() : "");
        String role = currentMember.getRole() != null ? currentMember.getRole() : "";
        String dept = currentMember.getDepartment() != null ? currentMember.getDepartment() : "";
        binding.tvSummaryRoleDept.setText(role + (role.isEmpty() || dept.isEmpty() ? "" : " · ") + dept);

        binding.tvSummaryAvatar.setText(currentMember.getInitials());
        int colorIndex = Math.abs((currentMember.getFullName() != null ? currentMember.getFullName() : "").hashCode()) % AVATAR_COLORS.length;
        binding.tvSummaryAvatar.setBackgroundTintList(ColorStateList.valueOf(AVATAR_COLORS[colorIndex]));

        updateStatusBadge(currentMember.getStatus());

        // Champs de saisie
        binding.etDetailName.setText(currentMember.getFullName());
        binding.etDetailPhone.setText(currentMember.getPhone());

        // Spinner statut
        if (Constants.STATUS_PENDING.equalsIgnoreCase(currentMember.getStatus())) {
            binding.spinnerDetailStatus.setSelection(1);
        } else {
            binding.spinnerDetailStatus.setSelection(0);
        }

        // Génération du QR Code personnel (fond blanc, modules #0B1020)
        String qrToken = currentMember.getQrToken();
        if (qrToken != null && !qrToken.trim().isEmpty()) {
            Bitmap qrBitmap = QrUtils.generateQrBitmap(qrToken, 320);
            if (qrBitmap != null) {
                binding.ivQrCode.setImageBitmap(qrBitmap);
            }
        }
    }

    private void updateStatusBadge(String status) {
        if (Constants.STATUS_PENDING.equalsIgnoreCase(status)) {
            binding.tvSummaryStatus.setText(R.string.status_pending);
            binding.tvSummaryStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_pending_text));
            binding.tvSummaryStatus.setBackgroundResource(R.drawable.bg_badge_pending);
        } else {
            binding.tvSummaryStatus.setText(R.string.status_active);
            binding.tvSummaryStatus.setTextColor(ContextCompat.getColor(this, R.color.badge_active_text));
            binding.tvSummaryStatus.setBackgroundResource(R.drawable.bg_badge_active);
        }
    }

    private void saveMemberChanges() {
        if (currentMember == null) return;

        String name = binding.etDetailName.getText() != null ? binding.etDetailName.getText().toString().trim() : "";
        String phone = binding.etDetailPhone.getText() != null ? binding.etDetailPhone.getText().toString().trim() : "";
        String newStatus = statusOptions[binding.spinnerDetailStatus.getSelectedItemPosition()];

        boolean hasError = false;
        if (!Validators.isValidName(name)) {
            binding.etDetailName.setError("Nom invalide (minimum 3 caractères)");
            hasError = true;
        }

        if (!Validators.isValidPhone(phone)) {
            binding.etDetailPhone.setError("Numéro tunisien invalide (8 chiffres)");
            hasError = true;
        }

        if (hasError) {
            showError("Veuillez corriger les champs invalides");
            return;
        }

        setLoading(true);

        currentMember.setFullName(name);
        currentMember.setPhone(phone);
        currentMember.setStatus(newStatus);

        FirebaseRepository.getInstance().updateMember(currentMember, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setLoading(false);
                Toast.makeText(MemberDetailActivity.this, "Modifications enregistrées", Toast.LENGTH_SHORT).show();
                populateViews();
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Erreur d'enregistrement : " + error);
            }
        });
    }

    private void confirmDeleteMember() {
        if (currentMember == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer le membre")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce membre ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteMember())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteMember() {
        setLoading(true);
        FirebaseRepository.getInstance().deleteMember(currentMember.getId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setLoading(false);
                Toast.makeText(MemberDetailActivity.this, "Membre supprimé avec succès", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Erreur de suppression : " + error);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveDetail.setEnabled(!isLoading);
        binding.btnDeleteMember.setEnabled(!isLoading);
        binding.etDetailName.setEnabled(!isLoading);
        binding.etDetailPhone.setEnabled(!isLoading);
        binding.spinnerDetailStatus.setEnabled(!isLoading);
    }
}
