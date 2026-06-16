package net.xdob.pf4boot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * 轻量 class 引用扫描器。
 *
 * 第一版只扫描 class 字节中的已知内部类名字符串，用于识别典型平台 API 缺失。
 */
final class BytecodeDependencyScanner {
	private static final Map<String, String> KNOWN_CLASS_TO_MODULE;

	static {
		Map<String, String> mappings = new TreeMap<>();
		mappings.put("org/slf4j/LoggerFactory", "org.slf4j:slf4j-api");
		KNOWN_CLASS_TO_MODULE = Collections.unmodifiableMap(mappings);
	}

	private BytecodeDependencyScanner() {
	}

	static Set<String> findMissingKnownModules(Set<ResolvedArtifactInfo> artifacts, Set<String> availableModuleKeys) {
		Set<String> missing = new TreeSet<>();
		for (ResolvedArtifactInfo artifact : artifacts) {
			File file = artifact.getFile();
			if (file == null || !file.exists()) {
				continue;
			}
			for (Map.Entry<String, String> mapping : KNOWN_CLASS_TO_MODULE.entrySet()) {
				if (!availableModuleKeys.contains(mapping.getValue()) && containsClassReference(file, mapping.getKey())) {
					missing.add(mapping.getValue() + " required by " + artifact.displayName());
				}
			}
		}
		return missing;
	}

	private static boolean containsClassReference(File file, String internalClassName) {
		try {
			if (file.isDirectory()) {
				return directoryContains(file, internalClassName);
			}
			if (file.getName().endsWith(".jar")) {
				return jarContains(file, internalClassName);
			}
			return file.getName().endsWith(".class") && bytesContain(readAll(new FileInputStream(file)), internalClassName);
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean directoryContains(File directory, String internalClassName) throws IOException {
		File[] children = directory.listFiles();
		if (children == null) {
			return false;
		}
		for (File child : children) {
			if (child.isDirectory()) {
				if (directoryContains(child, internalClassName)) {
					return true;
				}
			} else if (child.getName().endsWith(".class") && bytesContain(readAll(new FileInputStream(child)), internalClassName)) {
				return true;
			}
		}
		return false;
	}

	private static boolean jarContains(File file, String internalClassName) throws IOException {
		try (JarInputStream jar = new JarInputStream(new FileInputStream(file))) {
			JarEntry entry;
			while ((entry = jar.getNextJarEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().endsWith(".class") && bytesContain(readAll(jar), internalClassName)) {
					return true;
				}
			}
		}
		return false;
	}

	private static byte[] readAll(InputStream input) throws IOException {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int read;
			while ((read = input.read(buffer)) >= 0) {
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		} finally {
			input.close();
		}
	}

	private static boolean bytesContain(byte[] bytes, String needle) {
		byte[] target = needle.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		outer:
		for (int i = 0; i <= bytes.length - target.length; i++) {
			for (int j = 0; j < target.length; j++) {
				if (bytes[i + j] != target[j]) {
					continue outer;
				}
			}
			return true;
		}
		return false;
	}
}
