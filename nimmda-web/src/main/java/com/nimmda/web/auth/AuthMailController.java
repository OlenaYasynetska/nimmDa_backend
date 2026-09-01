package com.nimmda.web.auth;

import com.nimmda.application.mail.SendAuthMailCommand;
import com.nimmda.application.mail.SendAuthMailUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthMailController {

    private final SendAuthMailUseCase sendAuthMailUseCase;

    public AuthMailController(SendAuthMailUseCase sendAuthMailUseCase) {
        this.sendAuthMailUseCase = sendAuthMailUseCase;
    }

    @PostMapping("/mail")
    public ResponseEntity<Map<String, Boolean>> send(@Valid @RequestBody AuthMailRequest request) {
        boolean sent = sendAuthMailUseCase.execute(
                new SendAuthMailCommand(request.to(), request.type(), request.link())
        );
        return ResponseEntity.ok(Map.of("sent", sent));
    }
}
