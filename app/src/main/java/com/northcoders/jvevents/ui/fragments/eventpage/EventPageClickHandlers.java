package com.northcoders.jvevents.ui.fragments.eventpage;

import android.view.View;
import android.widget.Toast;

import com.northcoders.jvevents.model.EventDTO;

// CURRENTLY NOT USED

public class EventPageClickHandlers {

    public void onEventClicked(View view, EventDTO event) {
        Toast.makeText(view.getContext(), "Event clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void onEditEventClicked(View view, EventDTO event) {
        Toast.makeText(view.getContext(), "Edit event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void onSeeAttendeesClicked(View view, EventDTO event) {
        Toast.makeText(view.getContext(), "See attendees for: " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
