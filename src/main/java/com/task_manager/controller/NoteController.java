package com.task_manager.controller;

import com.task_manager.model.Note;
import com.task_manager.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "https://task-manager-one-orcin.vercel.app")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @GetMapping("/home")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @PostMapping("/create")
    public Note createNote(@RequestBody Note note) {
        return noteRepository.save(note);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable int id, @RequestBody Note noteDetails) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        note.setDescription(noteDetails.getDescription());
        final Note updatedNote = noteRepository.save(note);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable int id) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
        noteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }
}
