package dev.bodewig.compilefromsource.gradle.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;

class CompileFromSourceTest {

	@Test
	void testCompiles() {
		System.out.println(FreeType.class);
	}

	@Test
	void hasResources() {
		assertNotNull(ClassLoader.getSystemClassLoader().getResource("library.properties"));
	}
}
