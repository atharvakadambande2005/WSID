
package com.atharva.oauth;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ProblemController {

    @PostMapping("/api/problem")
    public String receiveProblem(@RequestBody String problem) {
        return "Your problem reached the backend: " + problem;
    }
}
