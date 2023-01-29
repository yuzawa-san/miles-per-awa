package com.jyuzawa.miles_per_awa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public class MilesPerAwaTest {
	
	@Autowired
	WebTestClient client;
	
	@Test
	public void test() {
		client.get().uri("/hello").exchange().expectStatus().is4xxClientError();
	}

}
