package com.clubsync.ui.member;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clubsync.databinding.ItemEventMemberBinding;
import com.clubsync.model.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MemberEventAdapter extends RecyclerView.Adapter<MemberEventAdapter.EventViewHolder> {

    public interface OnEventRegistrationListener {
        void onRegister(Event event);
        void onUnregister(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final Set<String> registeredEventIds = new HashSet<>();
    private final OnEventRegistrationListener listener;

    private static final int[] DATE_BLOCK_COLORS = {
            0xFF6B4EFF, // Violet
            0xFFEA580C, // Orange
            0xFFE11D48, // Rouge / Rose
            0xFF0D9488  // Teal
    };

    public MemberEventAdapter(OnEventRegistrationListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> newEvents, Set<String> registeredIds) {
        this.events.clear();
        if (newEvents != null) {
            this.events.addAll(newEvents);
        }
        this.registeredEventIds.clear();
        if (registeredIds != null) {
            this.registeredEventIds.addAll(registeredIds);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventMemberBinding binding = ItemEventMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position), position);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventMemberBinding binding;

        EventViewHolder(ItemEventMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event, int position) {
            if (event == null) return;

            // Bloc date coloré
            binding.tvEventDay.setText(event.getDay());
            binding.tvEventMonth.setText(event.getMonth());
            int color = DATE_BLOCK_COLORS[position % DATE_BLOCK_COLORS.length];
            binding.dateBlock.setBackgroundTintList(ColorStateList.valueOf(color));

            // Titre & Sous-titre
            binding.tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "");
            String time = event.getTime();
            String loc = event.getLocation() != null ? event.getLocation() : "";
            int remaining = event.getRemainingPlaces();
            String placesStr = remaining + " place" + (remaining > 1 ? "s" : "");
            binding.tvEventSubtitle.setText(time + (loc.isEmpty() ? "" : " · " + loc) + " · " + placesStr);

            boolean isRegistered = registeredEventIds.contains(event.getId());

            if (isRegistered) {
                binding.tvBadgeRegistered.setVisibility(View.VISIBLE);
                binding.btnRegister.setVisibility(View.GONE);
                binding.tvBadgeFull.setVisibility(View.GONE);

                binding.tvBadgeRegistered.setOnClickListener(v -> {
                    if (listener != null) listener.onUnregister(event);
                });
            } else if (event.isFull()) {
                binding.tvBadgeFull.setVisibility(View.VISIBLE);
                binding.tvBadgeRegistered.setVisibility(View.GONE);
                binding.btnRegister.setVisibility(View.GONE);
            } else {
                binding.btnRegister.setVisibility(View.VISIBLE);
                binding.tvBadgeRegistered.setVisibility(View.GONE);
                binding.tvBadgeFull.setVisibility(View.GONE);

                binding.btnRegister.setOnClickListener(v -> {
                    if (listener != null) listener.onRegister(event);
                });
            }
        }
    }
}
