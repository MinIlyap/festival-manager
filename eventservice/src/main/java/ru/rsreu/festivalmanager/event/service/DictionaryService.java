package ru.rsreu.festivalmanager.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rsreu.festivalmanager.event.entity.event.Event;
import ru.rsreu.festivalmanager.event.entity.stand.Stand;
import ru.rsreu.festivalmanager.event.repository.EventRepository;
import ru.rsreu.festivalmanager.event.repository.StandRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final EventRepository eventRepository;
    private final StandRepository standRepository;

    @Transactional
    public Long createEventDictionary(String name) {
        Event event = Event.builder().name(name).build();
        return eventRepository.save(event).getId();
    }

    @Transactional
    public Long createStandDictionary(String name) {
        Stand stand = Stand.builder().name(name).build();
        return standRepository.save(stand).getId();
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Stand> getAllStands() {
        return standRepository.findAll();
    }
}