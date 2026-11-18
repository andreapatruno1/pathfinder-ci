package test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.pathfinder.FileGraphLoader;
import software.ulpgc.pathfinder.GraphContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileGraphLoaderTest {
	@TempDir
	Path tempDir;

	private File writeGraph(String... lines) throws IOException {
		Path file = tempDir.resolve("graph.csv");
		Files.write(file, List.of(lines));
		return file.toFile();
	}

	@Test
	void loadsValidGraphFromFile() throws IOException {
		GraphContainer container = new FileGraphLoader(writeGraph(
				"A,B,1.0",
				"B,C,2.0",
				"A,D,4.0",
				"C,D,1.0"
		)).load();

		assertEquals(List.of("A", "B", "C"), container.shortestPathBetween("A", "C"));
		assertEquals(3.0, container.pathWeightBetween("A", "C"));
		assertEquals(4.0, container.pathWeightBetween("A", "D"));
	}

	@Test
	void skipsLinesWithWrongNumberOfFields() throws IOException {
		GraphContainer container = new FileGraphLoader(writeGraph(
				"A,B,1.0",
				"B,C",
				"B,C,2.0,extra"
		)).load();

		assertEquals(1.0, container.pathWeightBetween("A", "B"));
		assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "C"));
	}

	@Test
	void skipsLinesWithInvalidWeight() throws IOException {
		GraphContainer container = new FileGraphLoader(writeGraph(
				"A,B,notANumber",
				"A,C,5.5"
		)).load();

		assertEquals(5.5, container.pathWeightBetween("A", "C"));
		assertThrows(IllegalArgumentException.class, () -> container.pathWeightBetween("A", "B"));
	}

	@Test
	void emptyFileProducesEmptyGraph() throws IOException {
		GraphContainer container = new FileGraphLoader(writeGraph()).load();

		assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "B"));
	}

	@Test
	void missingFileThrowsIOException() {
		File missing = tempDir.resolve("does-not-exist.csv").toFile();

		assertThrows(IOException.class, () -> new FileGraphLoader(missing).load());
	}
}
