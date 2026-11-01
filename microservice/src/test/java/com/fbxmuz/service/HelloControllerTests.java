package com.fbxmuz.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloController.class)
class HelloControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void greetsWorldByDefault() throws Exception {
		mockMvc.perform(get("/hello"))
			.andExpect(status().isOk())
			.andExpect(content().string("Hello, World!"));
	}

	@Test
	void greetsByName() throws Exception {
		mockMvc.perform(get("/hello").param("name", "Ada"))
			.andExpect(status().isOk())
			.andExpect(content().string("Hello, Ada!"));
	}

}
