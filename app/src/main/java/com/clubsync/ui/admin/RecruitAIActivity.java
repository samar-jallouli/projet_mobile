package com.clubsync.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.clubsync.R;
import com.clubsync.data.AiService;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityRecruitAiBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.util.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecruitAIActivity extends BaseActivity {

    private ActivityRecruitAiBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    private final List<Member> pendingMembers = new ArrayList<>();
    private final List<String> recruitNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private String lastRecommendedDept = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecruitAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_members);
        setupSpinner();
        loadPendingRecruits();
        setupListeners();
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, recruitNames);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerRecruits.setAdapter(spinnerAdapter);
    }

    private void loadPendingRecruits() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getMembers(new Callback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> members) {
                binding.progressBar.setVisibility(View.GONE);
                pendingMembers.clear();
                recruitNames.clear();

                if (members != null) {
                    for (Member m : members) {
                        if (Constants.STATUS_PENDING.equalsIgnoreCase(m.getStatus())) {
                            pendingMembers.add(m);
                            recruitNames.add(m.getFullName() != null ? m.getFullName() : "Recrue");
                        }
                    }
                }

                if (pendingMembers.isEmpty()) {
                    recruitNames.add("Aucune recrue en attente");
                    binding.btnAnalyzeAI.setEnabled(false);
                } else {
                    binding.btnAnalyzeAI.setEnabled(true);
                }

                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                showError("Erreur de chargement des recrues : " + error);
            }
        });
    }

    private void setupListeners() {
        binding.btnAnalyzeAI.setOnClickListener(v -> analyzeRecruitProfile());
        binding.btnValidateDepartment.setOnClickListener(v -> confirmDepartmentValidation());
    }

    private void analyzeRecruitProfile() {
        if (pendingMembers.isEmpty() || binding.spinnerRecruits.getSelectedItemPosition() < 0) {
            showError("Veuillez sélectionner une recrue valide");
            return;
        }

        String skills = binding.etSkills.getText() != null ? binding.etSkills.getText().toString().trim() : "";
        String motivation = binding.etMotivation.getText() != null ? binding.etMotivation.getText().toString().trim() : "";

        if (skills.isEmpty() && motivation.isEmpty()) {
            showError("Veuillez renseigner au moins les compétences ou la motivation");
            return;
        }

        setLoading(true);

        AiService.getInstance().recommendDepartment(skills, motivation, new Callback<String>() {
            @Override
            public void onSuccess(String rawJson) {
                setLoading(false);
                parseAndDisplayRecommendation(rawJson);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Échec de l'analyse IA : " + error);
            }
        });
    }

    private void parseAndDisplayRecommendation(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            showError("Réponse IA vide");
            return;
        }

        try {
            // Nettoyage Markdown
            String cleaned = rawResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JSONObject json = new JSONObject(cleaned);
            String department = json.optString("department", Constants.DEPT_TECH);
            int score = json.optInt("score", 85);
            String reason = json.optString("reason", "Profil analysé par l'IA.");

            lastRecommendedDept = department;

            binding.tvRecommendedDepartment.setText(department);
            binding.tvScore.setText(score + " / 100");
            binding.pbScore.setProgress(Math.max(0, Math.min(100, score)));
            binding.tvReason.setText(reason);

            binding.cardResult.setVisibility(View.VISIBLE);
            binding.btnValidateDepartment.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            // Fallback si le format JSON n'est pas strict
            lastRecommendedDept = Constants.DEPT_TECH;
            binding.tvRecommendedDepartment.setText(Constants.DEPT_TECH);
            binding.tvScore.setText("80 / 100");
            binding.pbScore.setProgress(80);
            binding.tvReason.setText(rawResponse);

            binding.cardResult.setVisibility(View.VISIBLE);
            binding.btnValidateDepartment.setVisibility(View.VISIBLE);
        }
    }

    private void confirmDepartmentValidation() {
        int selectedIndex = binding.spinnerRecruits.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= pendingMembers.size()) return;

        Member member = pendingMembers.get(selectedIndex);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Validation du pôle")
                .setMessage("Affecter « " + member.getFullName() + " » au pôle " + lastRecommendedDept + " ?")
                .setPositiveButton("Valider", (dialog, which) -> {
                    setLoading(true);
                    member.setDepartment(lastRecommendedDept);
                    repository.updateMember(member, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            setLoading(false);
                            Toast.makeText(RecruitAIActivity.this, "Pôle mis à jour avec succès !", Toast.LENGTH_SHORT).show();
                            loadPendingRecruits();
                            binding.cardResult.setVisibility(View.GONE);
                            binding.btnValidateDepartment.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String error) {
                            setLoading(false);
                            showError("Erreur : " + error);
                        }
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnAnalyzeAI.setEnabled(!isLoading);
        binding.btnValidateDepartment.setEnabled(!isLoading);
    }
}
