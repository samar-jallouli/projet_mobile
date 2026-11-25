package com.clubsync.ui.member;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.clubsync.R;
import com.clubsync.data.AiService;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityAiAssistantBinding;
import com.clubsync.model.ChatMessage;
import com.clubsync.model.Event;
import com.clubsync.ui.common.BaseActivity;

import java.util.List;

public class AiAssistantActivity extends BaseActivity {

    private ActivityAiAssistantBinding binding;
    private ChatAdapter chatAdapter;
    private String preloadedEventsSummary = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiAssistantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_member_ai);
        setupChatRecyclerView();
        setupListeners();
        loadUpcomingEventsContext();

        // Message d'accueil initial
        chatAdapter.addMessage(new ChatMessage("Bonjour ! Je suis l'assistant de ClubSync. Comment puis-je vous aider aujourd'hui ?", false));
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        binding.btnSend.setOnClickListener(v -> sendUserMessage());

        binding.etQuestion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendUserMessage();
                return true;
            }
            return false;
        });

        binding.chipSuggestAttendance.setOnClickListener(v -> {
            binding.etQuestion.setText("Comment puis-je valider ma présence aux réunions du club ?");
            sendUserMessage();
        });

        binding.chipSuggestEvents.setOnClickListener(v -> {
            binding.etQuestion.setText("Quels sont les événements et réunions prévus ?");
            sendUserMessage();
        });
    }

    private void loadUpcomingEventsContext() {
        FirebaseRepository.getInstance().getEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                StringBuilder sb = new StringBuilder();
                if (events != null && !events.isEmpty()) {
                    for (Event e : events) {
                        sb.append("- ").append(e.getTitle()).append(" : ")
                                .append(e.getFormattedDate()).append(", ")
                                .append(e.getLocation() != null ? e.getLocation() : "lieu non défini")
                                .append(" (").append(e.getRemainingPlaces()).append(" places restantes)\n")
                                .append("  Ordre du jour : ").append(e.getDescription() != null ? e.getDescription() : "aucun")
                                .append("\n");
                    }
                }
                preloadedEventsSummary = sb.toString();
            }

            @Override
            public void onError(String error) {
                preloadedEventsSummary = "Aucun événement disponible actuellement.";
            }
        });
    }

    private void sendUserMessage() {
        String question = binding.etQuestion.getText() != null ? binding.etQuestion.getText().toString().trim() : "";
        if (question.isEmpty()) return;

        // Afficher message utilisateur
        chatAdapter.addMessage(new ChatMessage(question, true));
        binding.rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        binding.etQuestion.setText("");

        // Afficher indicateur
        setAiTyping(true);

        AiService.getInstance().askAssistant(question, preloadedEventsSummary, new Callback<String>() {
            @Override
            public void onSuccess(String response) {
                setAiTyping(false);
                chatAdapter.addMessage(new ChatMessage(response, false));
                binding.rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onError(String error) {
                setAiTyping(false);
                chatAdapter.addMessage(new ChatMessage("Désolé, je rencontre une difficulté : " + error, false));
                binding.rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void setAiTyping(boolean isTyping) {
        binding.tvTypingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
        binding.btnSend.setEnabled(!isTyping);
        binding.etQuestion.setEnabled(!isTyping);
    }
}
