
package com.atharva.oauth;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class WelcomeController {

    @GetMapping("/api/welcome")
    public String welcome() {
        return "Welcome to WSID - What Should I Do?";
    }
}
