package net.xdob.pf4boot;

import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 依赖解析与报告构造工具。
 */
final class DependencyReporter {
	static final String SOURCE_BUNDLE = "bundle";
	static final String SOURCE_BUNDLE_ONLY = "bundleOnly";
	static final String SOURCE_EMBED = "embed";
	static final String SOURCE_PLATFORM = "platform";
	static final String SOURCE_LOCAL_RUNTIME = "localRuntime";

	private DependencyReporter() {
	}

	static DependencyReport buildReport(
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed,
			Configuration platformClasspath,
			Configuration pluginLocalRuntimeClasspath
	) {
		Set<ResolvedArtifactInfo> bundleArtifacts = resolveArtifacts(bundle, SOURCE_BUNDLE);
		Set<ResolvedArtifactInfo> bundleOnlyArtifacts = resolveArtifacts(bundleOnly, SOURCE_BUNDLE_ONLY);
		Set<ResolvedArtifactInfo> embedArtifacts = resolveArtifacts(embed, SOURCE_EMBED);
		Set<ResolvedArtifactInfo> platformArtifacts = resolveArtifacts(platformClasspath, SOURCE_PLATFORM);
		Set<ResolvedArtifactInfo> localRuntimeArtifacts = resolveArtifacts(pluginLocalRuntimeClasspath, SOURCE_LOCAL_RUNTIME);

		Set<String> packagedModuleKeys = moduleKeys(bundleArtifacts, bundleOnlyArtifacts, embedArtifacts);
		Set<String> platformModuleKeys = moduleKeys(platformArtifacts);
		Set<String> localRuntimeModuleKeys = moduleKeys(localRuntimeArtifacts);

		Set<String> duplicates = new TreeSet<>(packagedModuleKeys);
		duplicates.retainAll(platformModuleKeys);

		Set<ResolvedArtifactInfo> missingPlatformArtifacts = platformArtifacts.stream()
				.filter(ResolvedArtifactInfo::hasModuleIdentity)
				.filter(artifact -> !localRuntimeModuleKeys.contains(artifact.moduleKey()))
				.collect(Collectors.toCollection(TreeSet::new));

		return new DependencyReport(
				bundleArtifacts,
				bundleOnlyArtifacts,
				embedArtifacts,
				platformArtifacts,
				localRuntimeArtifacts,
				duplicates,
				missingPlatformArtifacts
		);
	}

	static String formatReport(DependencyReport report) {
		StringBuilder out = new StringBuilder();
		out.append("pf4boot dependency report").append(System.lineSeparator()).append(System.lineSeparator());
		appendArtifacts(out, "Packaged bundle dependencies", report.getBundleArtifacts());
		appendArtifacts(out, "Packaged bundleOnly dependencies", report.getBundleOnlyArtifacts());
		appendArtifacts(out, "Packaged embed dependencies", report.getEmbedArtifacts());
		appendArtifacts(out, "Platform dependencies", report.getPlatformArtifacts());
		appendArtifacts(out, "Local runtime dependencies", report.getLocalRuntimeArtifacts());
		appendStrings(out, "Duplicated between packaged and platform", report.getDuplicateModuleKeys());
		appendArtifacts(out, "Missing platform dependencies in local runtime", report.getMissingPlatformArtifactsInLocalRuntime());
		return out.toString();
	}

	static void logDuplicateWarnings(Logger logger, DependencyReport report) {
		for (String duplicate : report.getDuplicateModuleKeys()) {
			logger.warn("Duplicate pf4boot dependency found in packaged and platform classpaths: {}", duplicate);
		}
	}

	private static Set<ResolvedArtifactInfo> resolveArtifacts(Configuration configuration, String source) {
		try {
			Set<ResolvedArtifactInfo> artifacts = new TreeSet<>();
			Set<File> artifactFiles = new HashSet<>();
			for (ResolvedArtifact artifact : configuration.getResolvedConfiguration().getResolvedArtifacts()) {
				File file = artifact.getFile();
				artifactFiles.add(file);
				artifacts.add(new ResolvedArtifactInfo(
						artifact.getModuleVersion().getId().getGroup(),
						artifact.getName(),
						artifact.getModuleVersion().getId().getVersion(),
						artifact.getClassifier(),
						artifact.getExtension(),
						file,
						source
				));
			}

			for (File file : configuration.getFiles()) {
				if (!artifactFiles.contains(file)) {
					artifacts.add(new ResolvedArtifactInfo(
							"file",
							file.getName(),
							"",
							"",
							extension(file),
							file,
							source
					));
				}
			}
			return artifacts;
		} catch (RuntimeException e) {
			throw new GradleException("Failed to resolve " + configuration.getName() + ": " + e.getMessage(), e);
		}
	}

	@SafeVarargs
	private static Set<String> moduleKeys(Set<ResolvedArtifactInfo>... artifactSets) {
		Set<String> moduleKeys = new TreeSet<>();
		for (Set<ResolvedArtifactInfo> artifactSet : artifactSets) {
			for (ResolvedArtifactInfo artifact : artifactSet) {
				if (artifact.hasModuleIdentity()) {
					moduleKeys.add(artifact.moduleKey());
				}
			}
		}
		return moduleKeys;
	}

	private static void appendArtifacts(StringBuilder out, String title, Set<ResolvedArtifactInfo> artifacts) {
		out.append(title).append(":").append(System.lineSeparator());
		if (artifacts.isEmpty()) {
			out.append("  <none>").append(System.lineSeparator()).append(System.lineSeparator());
			return;
		}
		for (ResolvedArtifactInfo artifact : artifacts) {
			out.append("  - ").append(artifact.displayName()).append(System.lineSeparator());
		}
		out.append(System.lineSeparator());
	}

	private static void appendStrings(StringBuilder out, String title, Set<String> values) {
		out.append(title).append(":").append(System.lineSeparator());
		if (values.isEmpty()) {
			out.append("  <none>").append(System.lineSeparator()).append(System.lineSeparator());
			return;
		}
		for (String value : values) {
			out.append("  - ").append(value).append(System.lineSeparator());
		}
		out.append(System.lineSeparator());
	}

	private static String extension(File file) {
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) {
			return "";
		}
		return name.substring(dot + 1);
	}
}
