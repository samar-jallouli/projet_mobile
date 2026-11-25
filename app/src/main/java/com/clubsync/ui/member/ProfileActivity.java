package com.clubsync.ui.member;

import android.os.Bundle;

import com.clubsync.R;
import com.clubsync.data.Callback;
import com.clubsync.data.FirebaseRepository;
import com.clubsync.databinding.ActivityProfileBinding;
import com.clubsync.model.Attendance;
import com.clubsync.model.Member;
import com.clubsync.ui.common.BaseActivity;

import java.util.List;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private final FirebaseRepository repository = FirebaseRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav(R.id.nav_member_profile);
        loadProfileData();
    }

    private void loadProfileData() {
        repository.getCurrentMember(new Callback<Member>() {
            @Override
            public void onSuccess(Member member) {
                if (member == null) return;

                String name = member.getFullName() != null ? member.getFullName() : "Membre";
                String dept = member.getDepartment() != null ? member.getDepartment() : "Tech";
                String role = member.getRole() != null ? member.getRole() : "Développeur";

                binding.tvProfileSubtitle.setText("Membre · Pôle " + dept);
                binding.tvProfileAvatar.setText(member.getInitials());
                binding.tvProfileName.setText(name);
                binding.tvProfileRoleActive.setText(role + " · membre actif");
                binding.tvLeaderboardMe.setText("4. " + name + " (moi)");

                loadMemberAttendances(member.getId());
            }

            @Override
            public void onError(String error) {
                showError("Erreur chargement profil : " + error);
            }
        });
    }

    private void loadMemberAttendances(String memberId) {
        repository.getAttendancesForMember(memberId, new Callback<List<Attendance>>() {
            @Override
            public void onSuccess(List<Attendance> attendances) {
                int count = (attendances != null && !attendances.isEmpty()) ? attendances.size() : 12;
                binding.tvProfilePresences.setText(String.valueOf(count));
                binding.tvLeaderboardMeScore.setText(String.valueOf(count));
            }

            @Override
            public void onError(String error) {
            }
        });
    }
}
