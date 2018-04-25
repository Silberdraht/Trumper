package de.hska.lkit.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;

import de.hska.lkit.demo.web.SpringWebDemoApplication;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringWebDemoApplication.class)
@WebAppConfiguration
public class SpringWebDemoApplicationTests {

	@Test
	public void contextLoads() {
	}

}
