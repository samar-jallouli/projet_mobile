package com.clubsync.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityAdminDashboardBinding;
import com.clubsync.model.Event;
import com.clubsync.ui.auth.LoginActivity;
import com.clubsync.ui.common.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class AdminDashboardActivity extends BaseActivity {

    private ActivityAdminDashboardBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();
    private ListenerRegistration eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_home);
        setupShortcutListeners();

        binding.tvAvatar.setOnClickListener(v -> showAdminMenu());
        binding.cardNextEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });
    }

    private void setupShortcutListeners() {
        binding.shortcutAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMemberActivity.class);
            startActivity(intent);
        });

        binding.shortcutNewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });

        binding.shortcutScanner.setOnClickListener(v -> {
            Intent intent = new Intent(this, AttendanceScannerActivity.class);
            startActivity(intent);
        });

        binding.shortcutAI.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecruitAIActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshKpiCounts();
        loadUpcomingEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    private void refreshKpiCounts() {
        repository.getActiveMembersCount(new Callback<Long>() {
            @Override
            public void onSuccess(Long count) {
                binding.tvActiveMembersCount.setText(String.valueOf(count != null ? count : 0));
            }

            @Override
            public void onError(String error) {
                // Reste sur valeur précédente ou indicateur
            }
        });

        repository.getPendingMembersCount(new Callback<Long>() {
            @Override
            public void onSuccess(Long count) {
                binding.tvPendingMembersCount.setText(String.valueOf(count != null ? count : 0));
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private void loadUpcomingEvent() {
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = repository.listenEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Event nextEvent = findNextUpcomingEvent(events);
                if (nextEvent != null) {
                    binding.tvNextEventDay.setText(nextEvent.getDay());
                    binding.tvNextEventMonth.setText(nextEvent.getMonth());
                    binding.tvNextEventTitle.setText(nextEvent.getTitle());
                    String subtitle = nextEvent.getTime() + (nextEvent.getLocation() != null && !nextEvent.getLocation().isEmpty() ? " · " + nextEvent.getLocation() : "");
                    binding.tvNextEventSubtitle.setText(subtitle);
                    binding.tvNextEventBadge.setText(nextEvent.getRegisteredCount() + " inscrits");
                } else {
                    binding.tvNextEventDay.setText("--");
                    binding.tvNextEventMonth.setText("--");
                    binding.tvNextEventTitle.setText("Aucun événement prévu");
                    binding.tvNextEventSubtitle.setText("Planning à venir");
                    binding.tvNextEventBadge.setText("0 inscrits");
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
        return events.get(events.size() - 1);
    }

    private void showAdminMenu() {
        String[] options = {"Générer les données de démo", "Se déconnecter"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Options Administrateur")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        populateDemo();
                    } else if (which == 1) {
                        showLogoutDialog();
                    }
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void populateDemo() {
        repository.populateDemoData(new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showSuccess("Données de démo créées (6 membres, 2 événements)");
                refreshKpiCounts();
                loadUpcomingEvent();
            }

            @Override
            public void onError(String error) {
                showError("Erreur : " + error);
            }
        });
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
