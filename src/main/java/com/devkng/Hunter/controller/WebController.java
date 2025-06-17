package com.devkng.Hunter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")  // root path
    public String showDashboard(Model model) {
        return "index"; // this means src/main/resources/templates/index.html
    }
    @GetMapping("/ssh")
    public String sshPage() {
        return "ssh"; // This resolves to ssh.html (or .jsp) in templates
    }

    @GetMapping("/outbound")
    public String outboundPage() {
        return "outbound";
    }

    @GetMapping("/config")
    public String configPage() {
        return "config";
    }
}
