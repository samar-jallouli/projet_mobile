package com.clubsync.ui.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityAttendanceScannerBinding;
import com.clubsync.model.Event;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.util.DateUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceScannerActivity extends BaseActivity {

    private static final int CAMERA_PERMISSION_CODE = 1001;

    private ActivityAttendanceScannerBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    private final List<Event> availableEvents = new ArrayList<>();
    private final List<String> eventTitles = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private BarcodeScanner barcodeScanner;

    private long lastScannedTimestamp = 0;
    private boolean isProcessingScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_admin_scanner);
        setupSpinner();
        loadEvents();

        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient();

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void setupSpinner() {
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, eventTitles);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.spinnerEvents.setAdapter(spinnerAdapter);
    }

    private void loadEvents() {
        repository.getEvents(new Callback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                availableEvents.clear();
                eventTitles.clear();
                if (events != null && !events.isEmpty()) {
                    availableEvents.addAll(events);
                    for (Event e : events) {
                        String dateStr = e.getDay() + "/" + e.getMonth() + " " + e.getTime();
                        eventTitles.add(e.getTitle() + " · " + dateStr);
                    }
                } else {
                    eventTitles.add("Aucun événement disponible");
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                showError("Erreur chargement événements : " + error);
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                showError("La permission Caméra est requise pour scanner les QR codes.");
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                showError("Impossible de démarrer la caméra : " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            showError("Erreur d'attachement de la caméra : " + e.getMessage());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && !rawValue.trim().isEmpty()) {
                            runOnUiThread(() -> onQrTokenDetected(rawValue.trim()));
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void onQrTokenDetected(String qrToken) {
        long now = System.currentTimeMillis();
        if (now - lastScannedTimestamp < 3500 || isProcessingScan) {
            return;
        }

        if (availableEvents.isEmpty() || binding.spinnerEvents.getSelectedItemPosition() < 0) {
            showError("Veuillez sélectionner un événement");
            return;
        }

        Event selectedEvent = availableEvents.get(binding.spinnerEvents.getSelectedItemPosition());
        isProcessingScan = true;
        lastScannedTimestamp = now;

        repository.getMemberByQrToken(qrToken, new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                String adminUid = repository.getCurrentUser() != null ? repository.getCurrentUser().getUid() : "admin";
                repository.recordAttendance(selectedEvent.getId(), member.getId(), member.getFullName(), adminUid, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        isProcessingScan = false;
                        showScanSuccess(member);
                    }

                    @Override
                    public void onError(String error) {
                        isProcessingScan = false;
                        showError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                isProcessingScan = false;
                showError("QR Code non reconnu : " + error);
            }
        });
    }

    private void showScanSuccess(Member member) {
        String timeStr = DateUtils.getTime(Timestamp.now());
        binding.tvScanResultDetail.setText(member.getFullName() + " · " + timeStr);
        binding.cardScanResult.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Présence validée pour " + member.getFullName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}
