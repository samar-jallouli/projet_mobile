package com.clubsync.ui.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityEventBinding;
import com.clubsync.model.Event;
import com.clubsync.ui.common.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventActivity extends BaseActivity {

    private ActivityEventBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();
    private AdminEventAdapter adapter;
    private ListenerRegistration eventsListener;

    private final Calendar selectedCalendar = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_events);
        setupRecyclerView();
        setupDateTimePickers();
        setupActions();
    }

    private void setupRecyclerView() {
        adapter = new AdminEventAdapter(this::confirmDeleteEvent);
        binding.rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminEvents.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        binding.etEventDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                isDateSelected = true;
                binding.etEventDate.setText(String.format(Locale.FRENCH, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        binding.etEventTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);
                isTimeSelected = true;
                binding.etEventTime.setText(String.format(Locale.FRENCH, "%02d:%02d", hourOfDay, minute));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
            timePicker.show();
        });
    }

    private void setupActions() {
        binding.btnGenerateAgendaAI.setOnClickListener(v -> {
            String title = binding.etEventTitle.getText() != null ? binding.etEventTitle.getText().toString().trim() : "";
            String location = binding.etEventLocation.getText() != null ? binding.etEventLocation.getText().toString().trim() : "";
            if (title.isEmpty()) {
                binding.etEventTitle.setError("Saisissez un titre d'abord");
                showError("Veuillez renseigner le titre de l'événement pour générer l'ordre du jour");
                return;
            }

            setLoading(true);
            com.clubsync.data.AiService.getInstance().generateAgenda(title, location, new Callback<String>() {
                @Override
                public void onSuccess(String agenda) {
                    setLoading(false);
                    binding.etEventDescription.setText(agenda);
                    Toast.makeText(EventActivity.this, "Ordre du jour généré avec succès", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    setLoading(false);
                    showError("Échec de la génération IA : " + error);
                }
            });
        });

        binding.btnPlanEvent.setOnClickListener(v -> planEvent());
    }

    private void planEvent() {
        String title = binding.etEventTitle.getText() != null ? binding.etEventTitle.getText().toString().trim() : "";
        String location = binding.etEventLocation.getText() != null ? binding.etEventLocation.getText().toString().trim() : "";
        String capacityStr = binding.etEventCapacity.getText() != null ? binding.etEventCapacity.getText().toString().trim() : "";
        String description = binding.etEventDescription.getText() != null ? binding.etEventDescription.getText().toString().trim() : "";

        boolean hasError = false;
        if (title.isEmpty()) {
            binding.etEventTitle.setError("Titre obligatoire");
            hasError = true;
        }

        if (location.isEmpty()) {
            binding.etEventLocation.setError("Lieu obligatoire");
            hasError = true;
        }

        int capacity = 0;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                binding.etEventCapacity.setError("Capacité supérieure à 0");
                hasError = true;
            }
        } catch (NumberFormatException e) {
            binding.etEventCapacity.setError("Capacité invalide");
            hasError = true;
        }

        if (!isDateSelected) {
            binding.etEventDate.setError("Sélectionnez une date");
            hasError = true;
        }

        if (!isTimeSelected) {
            binding.etEventTime.setError("Sélectionnez une heure");
            hasError = true;
        }

        if (hasError) {
            showError("Veuillez remplir correctement tous les champs obligatoires");
            return;
        }

        setLoading(true);

        Date eventDate = selectedCalendar.getTime();
        Event event = new Event();
        event.setTitle(title);
        event.setLocation(location);
        event.setCapacity(capacity);
        event.setDescription(description);
        event.setEventDate(new Timestamp(eventDate));
        event.setRegisteredCount(0);
        event.setAttendeesCount(0);
        if (repository.getCurrentUser() != null) {
            event.setCreatedBy(repository.getCurrentUser().getUid());
        }

        repository.addEvent(event, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                setLoading(false);
                Toast.makeText(EventActivity.this, "Événement planifié avec succès", Toast.LENGTH_SHORT).show();
                resetForm();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                showError("Erreur : " + error);
            }
        });
    }

    private void resetForm() {
        binding.etEventTitle.setText("");
        binding.etEventLocation.setText("");
        binding.etEventCapacity.setText("");
        binding.etEventDate.setText("");
        binding.etEventTime.setText("");
        binding.etEventDescription.setText("");
        isDateSelected = false;
        isTimeSelected = false;
    }

    private void confirmDeleteEvent(Event event) {
        if (event == null) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Supprimer l'événement")
                .setMessage("Voulez-vous supprimer l'événement « " + event.getTitle() + " » ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    setLoading(true);
                    repository.deleteEvent(event.getId(), new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            setLoading(false);
                            Toast.makeText(EventActivity.this, "Événement supprimé", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStart() {
        super.onStart();
        startListeningEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    private void startListeningEvents() {
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = repository.listenEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                adapter.setEvents(events);
            }

            @Override
            public void onError(String error) {
                showError("Erreur d'écoute : " + error);
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPlanEvent.setEnabled(!isLoading);
        binding.btnGenerateAgendaAI.setEnabled(!isLoading);
    }
}
