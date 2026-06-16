package net.xdob.pf4boot;

import java.io.File;

/**
 * 已解析依赖的展示模型。
 */
final class ResolvedArtifactInfo implements Comparable<ResolvedArtifactInfo> {
	private final String group;
	private final String name;
	private final String version;
	private final String classifier;
	private final String extension;
	private final File file;
	private final String source;

	ResolvedArtifactInfo(
			String group,
			String name,
			String version,
			String classifier,
			String extension,
			File file,
			String source
	) {
		this.group = group == null ? "" : group;
		this.name = name == null ? "" : name;
		this.version = version == null ? "" : version;
		this.classifier = classifier == null ? "" : classifier;
		this.extension = extension == null ? "" : extension;
		this.file = file;
		this.source = source == null ? "" : source;
	}

	String getGroup() {
		return group;
	}

	String getName() {
		return name;
	}

	String getVersion() {
		return version;
	}

	File getFile() {
		return file;
	}

	String getSource() {
		return source;
	}

	String moduleKey() {
		if (group.isEmpty()) {
			return name;
		}
		return group + ":" + name;
	}

	String coordinate() {
		String key = moduleKey();
		if (version.isEmpty()) {
			return key;
		}
		return key + ":" + version;
	}

	String displayName() {
		if (!coordinate().isEmpty()) {
			return coordinate();
		}
		return file == null ? "<unknown>" : file.getName();
	}

	boolean hasModuleIdentity() {
		return !group.isEmpty() && !name.isEmpty();
	}

	@Override
	public int compareTo(ResolvedArtifactInfo other) {
		int byCoordinate = coordinate().compareTo(other.coordinate());
		if (byCoordinate != 0) {
			return byCoordinate;
		}
		int bySource = source.compareTo(other.source);
		if (bySource != 0) {
			return bySource;
		}
		String thisPath = file == null ? "" : file.getAbsolutePath();
		String otherPath = other.file == null ? "" : other.file.getAbsolutePath();
		return thisPath.compareTo(otherPath);
	}
}
