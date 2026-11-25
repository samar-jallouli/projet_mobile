package com.clubsync.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.clubsync.R;
import com.clubsync.databinding.ItemEventAdminBinding;
import com.clubsync.model.Event;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onDeleteClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventActionListener listener;

    public AdminEventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        if (newEvents != null) {
            this.events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventAdminBinding binding = ItemEventAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventAdminBinding binding;

        EventViewHolder(ItemEventAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event) {
            if (event == null) return;

            binding.tvEventDay.setText(event.getDay());
            binding.tvEventMonth.setText(event.getMonth());
            binding.tvEventTitle.setText(event.getTitle() != null ? event.getTitle() : "");

            String time = event.getTime();
            String loc = event.getLocation() != null ? event.getLocation() : "";
            binding.tvEventSubtitle.setText(time + (loc.isEmpty() ? "" : " · " + loc));

            // Badge inscrits / capacité (ex: 8 / 30)
            binding.tvEventCapacityBadge.setText(event.getRegisteredCount() + " / " + event.getCapacity());

            // Griser les événements passés
            boolean isPast = event.getEventDate() != null && event.getEventDate().compareTo(Timestamp.now()) < 0;
            if (isPast) {
                binding.getRoot().setAlpha(0.55f);
                binding.tvEventCapacityBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
            } else {
                binding.getRoot().setAlpha(1.0f);
                binding.tvEventCapacityBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.badge_active_text));
            }

            binding.btnDeleteEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(event);
                }
            });
        }
    }
}
