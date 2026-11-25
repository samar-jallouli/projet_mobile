package com.clubsync.ui.common;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.clubsync.R;
import com.clubsync.ui.admin.AdminDashboardActivity;
import com.clubsync.ui.admin.AnalyticsActivity;
import com.clubsync.ui.admin.AttendanceScannerActivity;
import com.clubsync.ui.admin.EventActivity;
import com.clubsync.ui.member.AiAssistantActivity;
import com.clubsync.ui.member.MemberEventsActivity;
import com.clubsync.ui.member.MemberHomeActivity;
import com.clubsync.ui.member.MyQrActivity;
import com.clubsync.ui.member.ProfileActivity;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity {

    public void showError(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null && message != null) {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.error_red));
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
            snackbar.show();
        }
    }

    public void showSuccess(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null && message != null) {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.success_green));
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.white));
            snackbar.show();
        }
    }

    public void showInfo(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null && message != null) {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.card_glass_bg));
            snackbar.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            snackbar.show();
        }
    }

    protected void setupBottomNav(int selectedId) {
        // Barre Admin
        View adminContainer = findViewById(R.id.bottomNavAdminContainer);
        if (adminContainer != null) {
            setupAdminNav(selectedId);
        }

        // Barre Membre
        View memberContainer = findViewById(R.id.bottomNavMemberContainer);
        if (memberContainer != null) {
            setupMemberNav(selectedId);
        }
    }

    private void setupAdminNav(int selectedId) {
        int[] itemIds = {
                R.id.nav_admin_home,
                R.id.nav_admin_members,
                R.id.nav_admin_events,
                R.id.nav_admin_scanner,
                R.id.nav_admin_stats
        };

        int[] iconBgIds = {
                R.id.nav_admin_home_icon_bg,
                R.id.nav_admin_members_icon_bg,
                R.id.nav_admin_events_icon_bg,
                R.id.nav_admin_scanner_icon_bg,
                R.id.nav_admin_stats_icon_bg
        };

        int[] iconIds = {
                R.id.nav_admin_home_icon,
                R.id.nav_admin_members_icon,
                R.id.nav_admin_events_icon,
                R.id.nav_admin_scanner_icon,
                R.id.nav_admin_stats_icon
        };

        int[] labelIds = {
                R.id.nav_admin_home_label,
                R.id.nav_admin_members_label,
                R.id.nav_admin_events_label,
                R.id.nav_admin_scanner_label,
                R.id.nav_admin_stats_label
        };

        for (int i = 0; i < itemIds.length; i++) {
            int itemId = itemIds[i];
            View itemView = findViewById(itemId);
            FrameLayout iconBg = findViewById(iconBgIds[i]);
            ImageView iconView = findViewById(iconIds[i]);
            TextView labelView = findViewById(labelIds[i]);

            if (itemView == null || iconBg == null || iconView == null || labelView == null) {
                continue;
            }

            boolean isSelected = (itemId == selectedId);
            if (isSelected) {
                iconBg.setBackgroundResource(R.drawable.bg_nav_pill_selected);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.white));
                labelView.setTextColor(ContextCompat.getColor(this, R.color.white));
                labelView.setTypeface(null, Typeface.BOLD);
            } else {
                iconBg.setBackgroundResource(R.drawable.bg_nav_pill_unselected);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.nav_unselected));
                labelView.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
                labelView.setTypeface(null, Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (itemId == selectedId) return;

                Class<?> targetActivity = null;
                if (itemId == R.id.nav_admin_home) {
                    targetActivity = AdminDashboardActivity.class;
                } else if (itemId == R.id.nav_admin_members) {
                    targetActivity = MemberListActivity.class;
                } else if (itemId == R.id.nav_admin_events) {
                    targetActivity = EventActivity.class;
                } else if (itemId == R.id.nav_admin_scanner) {
                    targetActivity = AttendanceScannerActivity.class;
                } else if (itemId == R.id.nav_admin_stats) {
                    targetActivity = AnalyticsActivity.class;
                }

                if (targetActivity != null) {
                    Intent intent = new Intent(this, targetActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }

    private void setupMemberNav(int selectedId) {
        int[] itemIds = {
                R.id.nav_member_home,
                R.id.nav_member_events,
                R.id.nav_member_qr,
                R.id.nav_member_ai,
                R.id.nav_member_profile
        };

        int[] iconBgIds = {
                R.id.nav_member_home_icon_bg,
                R.id.nav_member_events_icon_bg,
                R.id.nav_member_qr_icon_bg,
                R.id.nav_member_ai_icon_bg,
                R.id.nav_member_profile_icon_bg
        };

        int[] iconIds = {
                R.id.nav_member_home_icon,
                R.id.nav_member_events_icon,
                R.id.nav_member_qr_icon,
                R.id.nav_member_ai_icon,
                R.id.nav_member_profile_icon
        };

        int[] labelIds = {
                R.id.nav_member_home_label,
                R.id.nav_member_events_label,
                R.id.nav_member_qr_label,
                R.id.nav_member_ai_label,
                R.id.nav_member_profile_label
        };

        for (int i = 0; i < itemIds.length; i++) {
            int itemId = itemIds[i];
            View itemView = findViewById(itemId);
            FrameLayout iconBg = findViewById(iconBgIds[i]);
            ImageView iconView = findViewById(iconIds[i]);
            TextView labelView = findViewById(labelIds[i]);

            if (itemView == null || iconBg == null || iconView == null || labelView == null) {
                continue;
            }

            boolean isSelected = (itemId == selectedId);
            if (isSelected) {
                iconBg.setBackgroundResource(R.drawable.bg_nav_pill_selected);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.white));
                labelView.setTextColor(ContextCompat.getColor(this, R.color.white));
                labelView.setTypeface(null, Typeface.BOLD);
            } else {
                iconBg.setBackgroundResource(R.drawable.bg_nav_pill_unselected);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.nav_unselected));
                labelView.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
                labelView.setTypeface(null, Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (itemId == selectedId) return;

                Class<?> targetActivity = null;
                if (itemId == R.id.nav_member_home) {
                    targetActivity = MemberHomeActivity.class;
                } else if (itemId == R.id.nav_member_events) {
                    targetActivity = MemberEventsActivity.class;
                } else if (itemId == R.id.nav_member_qr) {
                    targetActivity = MyQrActivity.class;
                } else if (itemId == R.id.nav_member_ai) {
                    targetActivity = AiAssistantActivity.class;
                } else if (itemId == R.id.nav_member_profile) {
                    targetActivity = ProfileActivity.class;
                }

                if (targetActivity != null) {
                    Intent intent = new Intent(this, targetActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }
}
