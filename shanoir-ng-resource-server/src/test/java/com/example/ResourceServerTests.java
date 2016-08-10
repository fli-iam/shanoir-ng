package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.ResourceServer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ResourceServer.class)
public class ResourceServerTests {

	@Test
	public void contextLoads() {
	}

}
