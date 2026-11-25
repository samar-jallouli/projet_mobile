package com.clubsync.ui.common;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.clubsync.R;
import com.clubsync.databinding.ItemMemberBinding;
import com.clubsync.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(Member member);
    }

    private final List<Member> members = new ArrayList<>();
    private final OnMemberClickListener listener;
    private final boolean isClickable;

    private static final int[] AVATAR_COLORS = {
            0xFF6B4EFF, // Violet accent
            0xFF0D9488, // Teal
            0xFFEA580C, // Orange
            0xFFE11D48, // Rose / Pink
            0xFF7C3AED, // Violet foncé
            0xFF0284C7, // Bleu ciel
            0xFF059669  // Vert émeraude
    };

    public MemberAdapter(OnMemberClickListener listener, boolean isClickable) {
        this.listener = listener;
        this.isClickable = isClickable;
    }

    public void setMembers(List<Member> newMembers) {
        this.members.clear();
        if (newMembers != null) {
            this.members.addAll(newMembers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberBinding binding = ItemMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberBinding binding;

        MemberViewHolder(ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Member member) {
            if (member == null) return;

            // Nom
            binding.tvMemberName.setText(member.getFullName() != null ? member.getFullName() : "");

            // Rôle · Département
            String role = member.getRole() != null ? member.getRole() : "";
            String dept = member.getDepartment() != null ? member.getDepartment() : "";
            String subtitle = role + (role.isEmpty() || dept.isEmpty() ? "" : " · ") + dept;
            binding.tvMemberRoleDept.setText(subtitle);

            // Avatar avec initiales et couleur déterministe
            binding.tvAvatar.setText(member.getInitials());
            int colorIndex = Math.abs((member.getFullName() != null ? member.getFullName() : "").hashCode()) % AVATAR_COLORS.length;
            binding.tvAvatar.setBackgroundTintList(ColorStateList.valueOf(AVATAR_COLORS[colorIndex]));

            // Statut badge
            if (member.isPending()) {
                binding.tvMemberStatus.setText(R.string.status_pending);
                binding.tvMemberStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_pending_text));
                binding.tvMemberStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            } else {
                binding.tvMemberStatus.setText(R.string.status_active);
                binding.tvMemberStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_active_text));
                binding.tvMemberStatus.setBackgroundResource(R.drawable.bg_badge_active);
            }

            // Clic
            if (isClickable && listener != null) {
                itemView.setOnClickListener(v -> listener.onMemberClick(member));
            } else {
                itemView.setOnClickListener(null);
            }
        }
    }
}
