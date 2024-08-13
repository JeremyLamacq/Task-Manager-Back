package com.task_manager.controller;

import com.task_manager.model.Note;
import com.task_manager.repository.NoteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://task-manager-one-orcin.vercel.app")
public class NoteController {

    @Autowired
    private NoteRepository NoteRepository;

    @GetMapping("/home")
    public List<Note> getAllNotes() {
        return NoteRepository.findAll();
    }

     @PostMapping("/create")
    public ResponseEntity<?> createNote(@RequestBody Map<String, String> request) {
        String description = request.get("description");

        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Description cannot be empty");
        }

        Note note = new Note();
        note.setDescription(description);

        try {
            Note savedNote = NoteRepository.save(note);
            return ResponseEntity.ok(savedNote);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating note: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note noteDetails) {
        Note note = NoteRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        note.setDescription(noteDetails.getDescription());
        final Note updatedNote = NoteRepository.save(note);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        Note note = NoteRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        NoteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }
}
