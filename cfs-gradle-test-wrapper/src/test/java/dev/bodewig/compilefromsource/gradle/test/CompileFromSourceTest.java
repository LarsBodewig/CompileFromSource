package dev.bodewig.compilefromsource.gradle.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import dev.bodewig.jcoprocessor.JCoprocessManager;

class CompileFromSourceTest {

	@Test
	void testCompiles() {
		System.out.println(JCoprocessManager.class);
	}

}
