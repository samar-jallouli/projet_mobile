package com.clubsync.data;

import android.os.Handler;
import android.os.Looper;

import com.clubsync.util.Constants;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class AiService {
    private static volatile AiService instance;
    private final GenerativeModelFutures generativeModel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Prompts système stricts en français
    public static final String SYSTEM_PROMPT_ASSISTANT =
            "Tu es l'assistant officiel du club étudiant ClubSync. " +
            "Réponds de manière concise, polie et directe en français. " +
            "IMPORTANT : Réponds UNIQUEMENT à partir de la liste des événements fournie ci-dessous. " +
            "Si l'information demandée ne s'y trouve pas ou si tu n'es pas certain, réponds strictement : " +
            "\"Je ne sais pas, veuillez contacter un responsable du club.\" " +
            "N'invente rien.";

    public static final String SYSTEM_PROMPT_AGENDA =
            "Tu es un organisateur expert pour le club étudiant ClubSync. " +
            "Rédige un ordre du jour structuré et pertinent pour l'événement spécifié. " +
            "Exigences : " +
            "- Rédige exactement entre 5 et 7 points numérotés (1 à 7). " +
            "- Ton professionnel, clair et adapté au milieu associatif étudiant. " +
            "- Chaque point doit comporter un titre et une brève description (1 ligne). " +
            "- Ne fournis aucune introduction ni conclusion, uniquement la liste numérotée.";

    public static final String SYSTEM_PROMPT_RECOMMENDATION =
            "Tu es le responsable du recrutement du club étudiant ClubSync. " +
            "Analyse le profil de la recrue selon ses compétences et sa motivation. " +
            "Les départements disponibles sont strictement : Tech, Com, Event, RH. " +
            "RÈGLE ABSOLUE : Tu dois répondre UNIQUEMENT par un objet JSON valide, sans texte d'introduction, sans balise Markdown (pas de ```json), exactement au format : " +
            "{\"department\":\"Tech|Com|Event|RH\",\"score\":85,\"reason\":\"Explication claire en 2 phrases max\"}";

    private AiService() {
        GenerativeModel model = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel(Constants.AI_MODEL);
        this.generativeModel = GenerativeModelFutures.from(model);
    }

    public static AiService getInstance() {
        if (instance == null) {
            synchronized (AiService.class) {
                if (instance == null) {
                    instance = new AiService();
                }
            }
        }
        return instance;
    }

    public GenerativeModelFutures getModel() {
        return generativeModel;
    }

    public void askAssistant(String question, String upcomingEventsText, Callback<String> callback) {
        if (question == null || question.trim().isEmpty()) {
            if (callback != null) callback.onError("Question vide");
            return;
        }

        String fullPrompt = SYSTEM_PROMPT_ASSISTANT + "\n\n" +
                "ÉVÉNEMENTS À VENIR DU CLUB :\n" +
                (upcomingEventsText != null && !upcomingEventsText.trim().isEmpty() ? upcomingEventsText : "Aucun événement prévu.") + "\n\n" +
                "QUESTION DE L'UTILISATEUR :\n" + question.trim();

        sendPrompt(fullPrompt, callback);
    }

    public void generateAgenda(String title, String location, Callback<String> callback) {
        if (title == null || title.trim().isEmpty()) {
            if (callback != null) callback.onError("Titre de l'événement manquant");
            return;
        }

        String fullPrompt = SYSTEM_PROMPT_AGENDA + "\n\n" +
                "Événement : " + title.trim() + "\n" +
                "Lieu : " + (location != null && !location.trim().isEmpty() ? location.trim() : "Non spécifié");

        sendPrompt(fullPrompt, callback);
    }

    public void recommendDepartment(String skills, String motivation, Callback<String> callback) {
        if ((skills == null || skills.trim().isEmpty()) && (motivation == null || motivation.trim().isEmpty())) {
            if (callback != null) callback.onError("Compétences ou motivation requises");
            return;
        }

        String fullPrompt = SYSTEM_PROMPT_RECOMMENDATION + "\n\n" +
                "Compétences de la recrue : " + (skills != null ? skills.trim() : "Non spécifiées") + "\n" +
                "Motivation : " + (motivation != null ? motivation.trim() : "Non spécifiée");

        sendPrompt(fullPrompt, callback);
    }

    private void sendPrompt(String promptText, Callback<String> callback) {
        try {
            Content content = new Content.Builder().addText(promptText).build();
            ListenableFuture<GenerateContentResponse> future = generativeModel.generateContent(content);

            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String text = result != null ? result.getText() : null;
                    if (text == null || text.trim().isEmpty()) {
                        postError(callback, "Réponse IA vide.");
                    } else {
                        postSuccess(callback, text.trim());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    String msg = "Erreur de connexion avec l'IA.";
                    if (t instanceof TimeoutException) {
                        msg = "Le délai d'attente de réponse de l'IA a expiré.";
                    } else if (t.getMessage() != null) {
                        msg = "Erreur IA : " + t.getLocalizedMessage();
                    }
                    postError(callback, msg);
                }
            }, executor);
        } catch (Exception e) {
            postError(callback, "Impossible d'initialiser la requête IA : " + e.getLocalizedMessage());
        }
    }

    private void postSuccess(Callback<String> callback, String result) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private void postError(Callback<String> callback, String error) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }
}
