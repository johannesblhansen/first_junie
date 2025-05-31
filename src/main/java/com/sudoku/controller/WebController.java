package com.sudoku.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the web UI pages.
 */
@Controller
public class WebController {

    /**
     * Serves the main page of the application.
     * 
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}