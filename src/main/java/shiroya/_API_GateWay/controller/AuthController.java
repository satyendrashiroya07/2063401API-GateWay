package shiroya._API_GateWay.controller;

import org.springframework.web.bind.annotation.*;
import shiroya._API_GateWay.DTO.LoginRequest;
import shiroya._API_GateWay.Jwt.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return JwtUtil.generateToken(request.getUsername());
    }
}
