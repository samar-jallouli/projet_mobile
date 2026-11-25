package com.clubsync.ui.member;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityMemberEventsBinding;
import com.clubsync.model.Event;
import com.clubsync.model.Member;
import com.clubsync.model.Registration;
import com.clubsync.ui.common.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemberEventsActivity extends BaseActivity implements MemberEventAdapter.OnEventRegistrationListener {

    private ActivityMemberEventsBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    private Member currentMember;
    private MemberEventAdapter adapter;

    private final List<Event> allEvents = new ArrayList<>();
    private final Set<String> registeredEventIds = new HashSet<>();

    private boolean showOnlyMyRegistrations = false;

    private ListenerRegistration eventsListener;
    private ListenerRegistration registrationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_member_events);
        setupRecyclerView();
        setupChips();
        loadMemberAndStartListening();
    }

    private void setupRecyclerView() {
        adapter = new MemberEventAdapter(this);
        binding.rvMemberEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMemberEvents.setAdapter(adapter);
    }

    private void setupChips() {
        binding.chipUpcoming.setOnClickListener(v -> {
            showOnlyMyRegistrations = false;
            binding.chipUpcoming.setBackgroundResource(R.drawable.bg_chip_selected);
            binding.chipUpcoming.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.chipMyRegistrations.setBackgroundResource(R.drawable.bg_chip_unselected);
            binding.chipMyRegistrations.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            binding.tvSubtitle.setText("À venir");
            applyFilter();
        });

        binding.chipMyRegistrations.setOnClickListener(v -> {
            showOnlyMyRegistrations = true;
            binding.chipMyRegistrations.setBackgroundResource(R.drawable.bg_chip_selected);
            binding.chipMyRegistrations.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.chipUpcoming.setBackgroundResource(R.drawable.bg_chip_unselected);
            binding.chipUpcoming.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            binding.tvSubtitle.setText("Mes inscriptions");
            applyFilter();
        });
    }

    private void loadMemberAndStartListening() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getCurrentMember(new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                currentMember = member;
                startRealtimeListeners();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                showError("Erreur profil membre : " + error);
            }
        });
    }

    private void startRealtimeListeners() {
        // Écoute des événements
        eventsListener = repository.listenEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                binding.progressBar.setVisibility(View.GONE);
                allEvents.clear();
                if (events != null) {
                    allEvents.addAll(events);
                }
                applyFilter();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                showError("Erreur d'écoute des événements : " + error);
            }
        });

        // Écoute des inscriptions du membre
        if (currentMember != null) {
            registrationsListener = repository.listenMemberRegistrations(currentMember.getId(), new Callback<List<Registration>>() {
                @Override
                public void onSuccess(List<Registration> registrations) {
                    registeredEventIds.clear();
                    if (registrations != null) {
                        for (Registration reg : registrations) {
                            if (reg.getEventId() != null) {
                                registeredEventIds.add(reg.getEventId());
                            }
                        }
                    }
                    applyFilter();
                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }

    private void applyFilter() {
        List<Event> displayList = new ArrayList<>();
        for (Event event : allEvents) {
            if (showOnlyMyRegistrations) {
                if (registeredEventIds.contains(event.getId())) {
                    displayList.add(event);
                }
            } else {
                displayList.add(event);
            }
        }

        adapter.setEvents(displayList, registeredEventIds);
        binding.tvEmptyState.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
        if (displayList.isEmpty()) {
            binding.tvEmptyState.setText(showOnlyMyRegistrations ? "Aucune inscription active" : "Aucun événement prévu");
        }
    }

    @Override
    public void onRegister(Event event) {
        if (event == null || currentMember == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        repository.registerToEvent(event.getId(), currentMember.getId(), currentMember.getFullName(), new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MemberEventsActivity.this, "Inscription validée !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                showError(error);
            }
        });
    }

    @Override
    public void onUnregister(Event event) {
        if (event == null || currentMember == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Annuler l'inscription")
                .setMessage("Voulez-vous annuler votre inscription à « " + event.getTitle() + " » ?")
                .setPositiveButton("Se désinscrire", (dialog, which) -> {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    repository.unregisterFromEvent(event.getId(), currentMember.getId(), new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(MemberEventsActivity.this, "Désinscription effectuée", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            binding.progressBar.setVisibility(View.GONE);
                            showError("Erreur : " + error);
                        }
                    });
                })
                .setNegativeButton("Retour", null)
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
        if (registrationsListener != null) {
            registrationsListener.remove();
            registrationsListener = null;
        }
    }
}
