package com.example.demo.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

@RestController
@RequestMapping("/api/game")
// Allow React access
public class GameController {

    private int targetNumber;

    public GameController() {
        startNewGame();
    }

    @PostMapping("/reset")
    public String startNewGame() {
        this.targetNumber = new Random().nextInt(100) + 1;
        return "New game started! Guess a number between 1 and 100.";
    }

    @GetMapping("/guess")
    public String checkGuess(@RequestParam int guess) {
        if (guess > targetNumber) {
            return "Number is too high!";
        } else if (guess < targetNumber) {
            return "Number is too low!";
        } else {
            return "Yes! The number is correct!";
        }
    }
}
