package com.clubsync.ui.member;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityMyQrBinding;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;
import com.clubsync.util.Constants;
import com.clubsync.util.QrUtils;

public class MyQrActivity extends BaseActivity {

    private ActivityMyQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_member_qr);
        loadMemberQrData();
    }

    private void loadMemberQrData() {
        FirebaseRepository.getInstance().getCurrentMember(new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                if (member == null) return;

                binding.tvMyName.setText(member.getFullName() != null ? member.getFullName() : "");
                String role = member.getRole() != null ? member.getRole() : "";
                String dept = member.getDepartment() != null ? member.getDepartment() : "";
                binding.tvMyRoleDept.setText(role + (role.isEmpty() || dept.isEmpty() ? "" : " · ") + dept);

                if (Constants.STATUS_PENDING.equalsIgnoreCase(member.getStatus())) {
                    binding.tvMyStatus.setText(R.string.status_pending);
                    binding.tvMyStatus.setTextColor(ContextCompat.getColor(MyQrActivity.this, R.color.badge_pending_text));
                    binding.tvMyStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                } else {
                    binding.tvMyStatus.setText(R.string.status_active);
                    binding.tvMyStatus.setTextColor(ContextCompat.getColor(MyQrActivity.this, R.color.badge_active_text));
                    binding.tvMyStatus.setBackgroundResource(R.drawable.bg_badge_active);
                }

                String token = member.getQrToken();
                if (token != null && !token.trim().isEmpty()) {
                    Bitmap qr = QrUtils.generateQrBitmap(token, 450);
                    if (qr != null) {
                        binding.ivMyQrCode.setImageBitmap(qr);
                    }
                }
            }

            @Override
            public void onError(String error) {
                showError("Impossible de charger le QR Code : " + error);
            }
        });
    }
}
