package net.xdob.pf4boot;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * pf4boot 依赖边界诊断结果。
 */
final class DependencyReport {
	private final Set<ResolvedArtifactInfo> bundleArtifacts;
	private final Set<ResolvedArtifactInfo> bundleOnlyArtifacts;
	private final Set<ResolvedArtifactInfo> embedArtifacts;
	private final Set<ResolvedArtifactInfo> platformArtifacts;
	private final Set<ResolvedArtifactInfo> localRuntimeArtifacts;
	private final Set<String> duplicateModuleKeys;
	private final Set<ResolvedArtifactInfo> missingPlatformArtifactsInLocalRuntime;

	DependencyReport(
			Set<ResolvedArtifactInfo> bundleArtifacts,
			Set<ResolvedArtifactInfo> bundleOnlyArtifacts,
			Set<ResolvedArtifactInfo> embedArtifacts,
			Set<ResolvedArtifactInfo> platformArtifacts,
			Set<ResolvedArtifactInfo> localRuntimeArtifacts,
			Set<String> duplicateModuleKeys,
			Set<ResolvedArtifactInfo> missingPlatformArtifactsInLocalRuntime
	) {
		this.bundleArtifacts = immutableCopy(bundleArtifacts);
		this.bundleOnlyArtifacts = immutableCopy(bundleOnlyArtifacts);
		this.embedArtifacts = immutableCopy(embedArtifacts);
		this.platformArtifacts = immutableCopy(platformArtifacts);
		this.localRuntimeArtifacts = immutableCopy(localRuntimeArtifacts);
		this.duplicateModuleKeys = Collections.unmodifiableSet(new TreeSet<>(duplicateModuleKeys));
		this.missingPlatformArtifactsInLocalRuntime = immutableCopy(missingPlatformArtifactsInLocalRuntime);
	}

	Set<ResolvedArtifactInfo> getBundleArtifacts() {
		return bundleArtifacts;
	}

	Set<ResolvedArtifactInfo> getBundleOnlyArtifacts() {
		return bundleOnlyArtifacts;
	}

	Set<ResolvedArtifactInfo> getEmbedArtifacts() {
		return embedArtifacts;
	}

	Set<ResolvedArtifactInfo> getPlatformArtifacts() {
		return platformArtifacts;
	}

	Set<ResolvedArtifactInfo> getLocalRuntimeArtifacts() {
		return localRuntimeArtifacts;
	}

	Set<String> getDuplicateModuleKeys() {
		return duplicateModuleKeys;
	}

	Set<ResolvedArtifactInfo> getMissingPlatformArtifactsInLocalRuntime() {
		return missingPlatformArtifactsInLocalRuntime;
	}

	Set<ResolvedArtifactInfo> packagedArtifacts() {
		Set<ResolvedArtifactInfo> artifacts = new TreeSet<>();
		artifacts.addAll(bundleArtifacts);
		artifacts.addAll(bundleOnlyArtifacts);
		artifacts.addAll(embedArtifacts);
		return artifacts;
	}

	private Set<ResolvedArtifactInfo> immutableCopy(Set<ResolvedArtifactInfo> source) {
		return Collections.unmodifiableSet(new TreeSet<>(source));
	}
}
