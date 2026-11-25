package com.clubsync.ui.member;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityMemberHomeBinding;
import com.clubsync.model.Event;
import com.clubsync.model.Member;
import com.clubsync.ui.auth.LoginActivity;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.ui.common.MemberListActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MemberHomeActivity extends BaseActivity {

    private ActivityMemberHomeBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    private Member currentMember;
    private Event currentNextEvent;
    private boolean isRegisteredForNextEvent = false;

    private ListenerRegistration eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_member_home);
        setupListeners();
    }

    private void setupListeners() {
        binding.cardDirectory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemberListActivity.class);
            startActivity(intent);
        });

        binding.tvAvatar.setOnClickListener(v -> showLogoutDialog());

        binding.btnRegisterNextEvent.setOnClickListener(v -> handleNextEventRegistration());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMemberProfile();
        loadUpcomingEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    private void loadMemberProfile() {
        repository.getCurrentMember(new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                currentMember = member;
                if (member != null) {
                    String firstName = member.getFullName() != null ? member.getFullName().split("\\s+")[0] : "Membre";
                    binding.tvMemberGreeting.setText("Salut, " + firstName);
                    String dept = member.getDepartment() != null ? member.getDepartment() : "Général";
                    binding.tvMemberRoleSubtitle.setText("Membre · Pôle " + dept);
                    binding.tvAvatar.setText(member.getInitials());

                    // Vérifier l'inscription pour le prochain événement
                    checkRegistrationForCurrentNextEvent();
                }
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void loadUpcomingEvents() {
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = repository.listenEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                currentNextEvent = findNextUpcomingEvent(events);
                if (currentNextEvent != null) {
                    binding.tvNextEventTitle.setText(currentNextEvent.getTitle());
                    String subtitle = currentNextEvent.getDay() + " " + currentNextEvent.getMonth() + " · "
                            + currentNextEvent.getTime()
                            + (currentNextEvent.getLocation() != null ? " · " + currentNextEvent.getLocation() : "");
                    binding.tvNextEventDateTime.setText(subtitle);
                    checkRegistrationForCurrentNextEvent();
                } else {
                    binding.tvNextEventTitle.setText("Aucun événement prévu");
                    binding.tvNextEventDateTime.setText("Revenez plus tard pour le planning");
                    binding.btnRegisterNextEvent.setEnabled(false);
                }
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private Event findNextUpcomingEvent(List<Event> events) {
        if (events == null || events.isEmpty()) return null;
        Timestamp now = Timestamp.now();
        for (Event e : events) {
            if (e.getEventDate() != null && e.getEventDate().compareTo(now) >= 0) {
                return e;
            }
        }
        return events.get(0);
    }

    private void checkRegistrationForCurrentNextEvent() {
        if (currentNextEvent == null || currentMember == null) return;

        repository.checkRegistration(currentNextEvent.getId(), currentMember.getId(), new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean registered) {
                isRegisteredForNextEvent = registered != null && registered;
                updateRegistrationButtonUI();
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void updateRegistrationButtonUI() {
        if (isRegisteredForNextEvent) {
            binding.btnRegisterNextEvent.setText("Inscrit ✓");
            binding.btnRegisterNextEvent.setBackgroundResource(R.drawable.bg_button_registered);
            binding.btnRegisterNextEvent.setBackgroundTintList(null);
            binding.btnRegisterNextEvent.setTextColor(ContextCompat.getColor(this, R.color.badge_active_text));
        } else {
            binding.btnRegisterNextEvent.setText("Je m'inscris");
            binding.btnRegisterNextEvent.setBackgroundResource(R.drawable.bg_button_white);
            binding.btnRegisterNextEvent.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            binding.btnRegisterNextEvent.setTextColor(ContextCompat.getColor(this, R.color.primary_accent));
        }
        binding.btnRegisterNextEvent.setEnabled(true);
    }

    private void handleNextEventRegistration() {
        if (currentNextEvent == null || currentMember == null) return;

        if (isRegisteredForNextEvent) {
            // Confirmation de désinscription
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Annuler l'inscription")
                    .setMessage("Voulez-vous annuler votre inscription à « " + currentNextEvent.getTitle() + " » ?")
                    .setPositiveButton("Se désinscrire", (dialog, which) -> {
                        repository.unregisterFromEvent(currentNextEvent.getId(), currentMember.getId(), new Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                isRegisteredForNextEvent = false;
                                updateRegistrationButtonUI();
                                Toast.makeText(MemberHomeActivity.this, "Désinscription effectuée", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error) {
                                showError("Erreur : " + error);
                            }
                        });
                    })
                    .setNegativeButton("Retour", null)
                    .show();
        } else {
            // Inscription
            repository.registerToEvent(currentNextEvent.getId(), currentMember.getId(), currentMember.getFullName(), new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    isRegisteredForNextEvent = true;
                    updateRegistrationButtonUI();
                    Toast.makeText(MemberHomeActivity.this, "Inscription confirmée !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    showError(error);
                }
            });
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Déconnexion", (dialog, which) -> {
                    repository.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
