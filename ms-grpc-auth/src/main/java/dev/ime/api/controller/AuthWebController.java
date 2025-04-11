package dev.ime.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthWebController {

	@GetMapping("/success")
	public String successLogin() {
		return "success";
	}

	@GetMapping("/error")
	public String errorLogin() {
		return "error";
	}

	@GetMapping("/callback")
	public String handleCallback(@RequestParam("code") String authorizationCode, @RequestParam String state, Model model) {

		model.addAttribute("authorizationCode", authorizationCode);
		model.addAttribute("state", state);
		return "callback";
	}

}
