package generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Control {

	public static Question run(int level, int weight, ProblemType pt) {

		if (weight < 1) {
			weight = 1;
		}

		ProblemComponent problem = new ProblemComponent(level, weight, pt);
		ConverterHtml converter = new ConverterHtml();

		LinkedList<String> question = converter.convertProblem(problem,
				ComponentTypes.None, 2);

		LinkedList<Integer> spaces = new LinkedList<Integer>();
		LinkedList<String> lines = new LinkedList<String>();
		String current = null;
		try {
			current = question.remove();
		} catch (NoSuchElementException e) {
			current = null;
		}
		while (current != null) {
			int spaceCount = 0;
			while (current.startsWith("%")) {
				spaceCount++;
				current = current.substring(1);
			}
			lines.add(current);
			spaces.add(spaceCount);
			try {
				current = question.remove();

			} catch (NoSuchElementException e) {
				current = null;
			}
		}

		Question returnQuestion = null;

		if (pt == ProblemType.MULTI_CHOICE) {
			int[] answers = multipleChoiceAnswers(problem);
			returnQuestion = new Question(lines, spaces, answers);
		} else { // else do fill in the blank

			File tmp = null;
			// create random file name
			try {
				File root = new File("/export/home/mgoddard/CBCWebsite/temp");
				tmp = File.createTempFile("FIB", ".java", root);
			} catch (IOException e) {
				System.out.println("IOEXCPETION " + e);
				e.printStackTrace();
			}

			String userInput = readReplacement(problem);
			System.out.println("We be here");
			int returnedAnswer = runCompilerWithReplacement("2 == 2", problem,
					tmp);

			if (returnedAnswer == problem.getCorrectAnswer()) {
				int[] yes = { 1, 1, 1, 1 };
				returnQuestion = new Question(lines, spaces, yes);
			} else {
				int[] no = { 0, 0, 0, 0 };
				returnQuestion = new Question(lines, spaces, no);
			}
		}

		return returnQuestion;
	}

	private static int runCompilerWithReplacement(String replacement,
			ProblemComponent problem, File temp) {

		URL Url = ((URLClassLoader) (Thread.currentThread()
				.getContextClassLoader())).getURLs()[0];

		try {
			System.out.println("path: " + Url.toURI().toString());
		} catch (URISyntaxException e1) {
			System.out.println("e in conversion: " + e1);
			e1.printStackTrace();
		}

		System.out.println("control path: " + temp.getPath());
		System.out.println("name: " + temp.getName());

		JavaConverter javaConverter = new JavaConverter();
		javaConverter.convertProblem(problem,
				Difficulty.getProblemComponent(ProblemType.FILL_BLANK, 2),
				replacement, temp);

		File root = new File("/export/home/mgoddard/CBCWebsite/temp");
		File sourceFile = new File(root, "javaOutput.java");

		String fileToCompile = temp.getPath();
		String className = temp.getName();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		System.out.println("compiling");
		compiler.run(null, System.out, null, fileToCompile);
		System.out.println("finished");
		URLClassLoader classLoader;

		Class<?> cls = null;
		simpleInterface instance = null;
		try {
			classLoader = URLClassLoader.newInstance(new URL[] { root.toURI()
					.toURL() });
			cls = Class.forName(className, true, classLoader);

			instance = (simpleInterface) cls.newInstance();
		} catch (InstantiationException e) {
			System.out.println("INSTANTIATION EXCEPTION" + e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("ILLEGAL ACCESS EXCEPTION" + e);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			System.out.println("MALFORMED URL EXCPETION " + e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("CLASS NOT FOUND EXCEPTION " + e);
			e.printStackTrace();
		}
		int returnedAnswer = 2;
		// int returnedAnswer = instance.Main();
		System.out.println("With the inputed answer, the function returns "
				+ returnedAnswer);
		/*
		 * try { Files.delete(sourceFile.toPath()); } catch (IOException e1) {
		 * // TODO // Auto-generated catch block e1.printStackTrace(); }
		 */
		return returnedAnswer;
	}

	private static javaOutput runHelper(Object input) {
		return (javaOutput) input;
	}

	private static String readReplacement(ProblemComponent problem) {
		System.out
				.println("What should be placed into the ??? such that Main returns "
						+ problem.getCorrectAnswer());
		String line = null;
		/*
		 * try { line = br.readLine(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		return line;
	}

	private static int[] multipleChoiceAnswers(ProblemComponent problem) {
		int[] returnAnswers = new int[4];
		returnAnswers[0] = problem.getCorrectAnswer();
		returnAnswers[1] = problem.getIncorrect1();
		returnAnswers[2] = problem.getIncorrect2();
		returnAnswers[3] = problem.getIncorrect3();
		return returnAnswers;
	}
}
