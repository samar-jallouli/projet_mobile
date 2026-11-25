package com.clubsync.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityMemberListBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.admin.AddMemberActivity;
import com.clubsync.ui.admin.MemberDetailActivity;
import com.clubsync.util.Constants;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MemberListActivity extends BaseActivity {

    public static final String EXTRA_MEMBER_ID = "extra_member_id";

    private ActivityMemberListBinding binding;
    private final List<Member> allMembers = new ArrayList<>();
    private MemberAdapter adapter;
    private ListenerRegistration membersListener;

    private String currentDepartmentFilter = "Tous";
    private String currentSearchQuery = "";
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));

        determineRoleAndSetupUI();
        setupSearchAndFilters();
    }

    private void determineRoleAndSetupUI() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseRepository.getInstance().getCurrentUserRole(new Callback<String>() {
            @Override
            public void onSuccess(String role) {
                binding.progressBar.setVisibility(View.GONE);
                isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role);
                configureRoleSpecificViews();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                // Par défaut en lecture seule par sécurité
                isAdmin = false;
                configureRoleSpecificViews();
            }
        });
    }

    private void configureRoleSpecificViews() {
        // Configuration de l'adaptateur
        adapter = new MemberAdapter(member -> {
            if (isAdmin && member != null) {
                Intent intent = new Intent(MemberListActivity.this, MemberDetailActivity.class);
                intent.putExtra(EXTRA_MEMBER_ID, member.getId());
                startActivity(intent);
            }
        }, isAdmin);

        binding.rvMembers.setAdapter(adapter);

        // Bouton flottant d'ajout
        binding.fabAddMember.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (isAdmin) {
            binding.fabAddMember.setOnClickListener(v -> {
                Intent intent = new Intent(MemberListActivity.this, AddMemberActivity.class);
                startActivity(intent);
            });
        }

        // Configuration barre de navigation
        if (isAdmin) {
            binding.adminBottomNav.getRoot().setVisibility(View.VISIBLE);
            binding.memberBottomNav.getRoot().setVisibility(View.GONE);
            setupBottomNav(R.id.nav_admin_members);
        } else {
            binding.adminBottomNav.getRoot().setVisibility(View.GONE);
            binding.memberBottomNav.getRoot().setVisibility(View.VISIBLE);
            setupBottomNav(0);
        }

        applyFilters();
    }

    private void setupSearchAndFilters() {
        // Recherche instantanée
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clics sur les chips de filtre
        binding.chipAll.setOnClickListener(v -> selectChip("Tous", binding.chipAll));
        binding.chipTech.setOnClickListener(v -> selectChip(Constants.DEPT_TECH, binding.chipTech));
        binding.chipCom.setOnClickListener(v -> selectChip(Constants.DEPT_COM, binding.chipCom));
        binding.chipEvent.setOnClickListener(v -> selectChip(Constants.DEPT_EVENT, binding.chipEvent));
        binding.chipRH.setOnClickListener(v -> selectChip(Constants.DEPT_RH, binding.chipRH));
    }

    private void selectChip(String department, TextView selectedChipView) {
        currentDepartmentFilter = department;

        TextView[] allChips = {binding.chipAll, binding.chipTech, binding.chipCom, binding.chipEvent, binding.chipRH};
        for (TextView chip : allChips) {
            if (chip == selectedChipView) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }

        applyFilters();
    }

    private void applyFilters() {
        List<Member> filtered = new ArrayList<>();
        for (Member m : allMembers) {
            boolean matchesDept = "Tous".equalsIgnoreCase(currentDepartmentFilter)
                    || (m.getDepartment() != null && m.getDepartment().equalsIgnoreCase(currentDepartmentFilter));

            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || (m.getFullName() != null && m.getFullName().toLowerCase().contains(currentSearchQuery));

            if (matchesDept && matchesSearch) {
                filtered.add(m);
            }
        }

        if (adapter != null) {
            adapter.setMembers(filtered);
        }

        binding.tvMemberCount.setText(filtered.size() + " au total");
        binding.tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningMembers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopListeningMembers();
    }

    private void startListeningMembers() {
        if (membersListener != null) {
            membersListener.remove();
        }

        binding.progressBar.setVisibility(allMembers.isEmpty() ? View.VISIBLE : View.GONE);
        membersListener = FirebaseRepository.getInstance().listenMembers(new Callback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> result) {
                binding.progressBar.setVisibility(View.GONE);
                allMembers.clear();
                if (result != null) {
                    allMembers.addAll(result);
                }
                applyFilters();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                showError("Erreur d'écoute temps réel : " + error);
            }
        });
    }

    private void stopListeningMembers() {
        if (membersListener != null) {
            membersListener.remove();
            membersListener = null;
        }
    }
}
