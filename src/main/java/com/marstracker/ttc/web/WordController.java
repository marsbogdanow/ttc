package com.marstracker.ttc.web;

import com.marstracker.ttc.model.Word;
import com.marstracker.ttc.model.WordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/wordapi")
public class WordController {
    private final WordRepository wordRepository;
    private final Logger log = LoggerFactory.getLogger(WordController.class);

    public WordController(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<Word> add(@RequestBody Word word) throws URISyntaxException {
        log.info("Request to create word: {}", word);
        Word result = wordRepository.save(word);
        return ResponseEntity.created(new URI("/wordapi/add/" + result.getId())).body(result);
    }
    @GetMapping("/words")
    public List<Word> words() {
        log.info("Request to get all words.");
        return wordRepository.findAll();
    }

    @GetMapping("/word/{id}")
    ResponseEntity<?> getWord(@PathVariable Long id) {
        Optional<Word> group = wordRepository.findById(id);
        return group.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
