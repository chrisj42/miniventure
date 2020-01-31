package miniventure.game.util;

import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
	
	// the last digit increments without release or tag
	public static final Version CURRENT = makeVersion("2.2.1");
	
	// the last time there was a change in the save format
	private static final Version latestFormatChange = makeVersion("2.2.1");
	
	// use this to determine the latest version a world is compatible with
	private static final TreeMap<Version, Version> endOfSupportVersions = new TreeMap<>();
	static {
		// this is the oldest version that the current version supports
		endOfSupportVersions.put(makeVersion("2.2.1"), CURRENT);
		// below is for older versions; add an entry whenever a version loses support
		// no need to add an entry if it was only supported for one version
		// addEoSVersion("2.1.2", "2.1.2");
		// addEoSVersion("2.2.1.4", "2.2.1.4");
	}
	
	private static void addEoSVersion(String version, String lastSupportingVersion) {
		endOfSupportVersions.put(makeVersion(version), makeVersion(lastSupportingVersion));
	}
	
	/*
		knowing the latest format change shows when to highlight the world yellow.
		It can then say that "file format was changed in version x, your file will be migrated"
		
		the oldest supported version shows when to highlight it red.
		it can then say "version unsupported, support ended in version x"
	 */
	
	// returns the latest version that supports the given version
	@NotNull
	public static Version getSupportFor(@NotNull Version dataVersion) {
		Version floored = endOfSupportVersions.floorKey(dataVersion);
		Version support = floored == null ? null : endOfSupportVersions.get(floored);
		if(support == null || support.isBefore(dataVersion))
			return dataVersion; // unknown, or unspecified; assume data version
		return support;
	}
	
	public static boolean matchesCurrentFormat(@NotNull Version dataVersion) {
		return dataVersion.atOrAfter(latestFormatChange);
	}
	
	public static class VersionFormatException extends Exception {
		private VersionFormatException(String versionString, String reason, Throwable cause) {
			super("The string \"" + versionString + "\" does not represent a valid version format: "+reason, cause);
		}
	}
	private static class UncaughtVersionFormatException extends IllegalArgumentException {
		UncaughtVersionFormatException(VersionFormatException e) { super(e); }
	}
	private static Version makeVersion(String version) {
		try {
			return new Version(version);
		} catch (VersionFormatException e) {
			throw new UncaughtVersionFormatException(e);
		}
	}
	
	private final int make, major, minor, build;
	
	private Version() {make = 0; major = 0; minor = 0; build = 0;}
	public Version(String version) throws VersionFormatException {
		String[] nums = version.split("\\.");
		
		if(nums.length < 3 || nums.length > 4)
			throw new VersionFormatException(version, "3-4 numbers required", null);
		
		try {
			make = new Integer(nums[0]);
			major = new Integer(nums[1]);
			minor = new Integer(nums[2]);
			if(nums.length == 3)
				build = 0; // release build
			else
				build = new Integer(nums[3]);
			
		} catch(NumberFormatException ex) {
			throw new VersionFormatException(version, "", ex);
		}
		
		if(make < 0 || major < 0 || minor < 0 || build < 0)
			throw new VersionFormatException(version, "", null);
	}
	
	// returns a string representation in the same form as what is expected in the constructor.
	public String serialize() {
		return String.valueOf(make) + '.' + major + '.' + minor + '.' + build;
	}
	
	public boolean isBefore(String version) { return isBefore(makeVersion(version)); }
	public boolean isBefore(@NotNull Version version) {
		return compareTo(version) < 0;
	}
	public boolean atOrBefore(String version) { return atOrBefore(makeVersion(version)); }
	public boolean atOrBefore(@NotNull Version version) {
		return compareTo(version) <= 0;
	}
	
	public boolean isAfter(String version) { return isAfter(makeVersion(version)); }
	public boolean isAfter(@NotNull Version version) {
		return compareTo(version) > 0;
	}
	public boolean atOrAfter(String version) { return atOrAfter(makeVersion(version)); }
	public boolean atOrAfter(@NotNull Version version) {
		return compareTo(version) >= 0;
	}
	
	@Override
	public int compareTo(@NotNull Version other) {
		if(make != other.make) return Integer.compare(make, other.make);
		if(major != other.major) return Integer.compare(major, other.major);
		if(minor != other.minor) return Integer.compare(minor, other.minor);
		if(build != other.build) {	
			if(build == 0) return 1; // 0 means after all other numbers, so this is after the other version.
			if(other.build == 0) return -1; // this is before the other version.
			return Integer.compare(build, other.build); // compare normally.
		}
		
		return 0; // versions are the same.
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Version)) return false;
		Version o = (Version) other;
		return make==o.make && major==o.major && minor==o.minor && build==o.build;
	}
	
	@Override
	public int hashCode() {
		int result = make;
		result = 31 * result + major;
		result = 31 * result + minor;
		result = 17 * result + build;
		return result;
	}
	
	@Override
	public String toString() {
		// note: when major is 0, minor is ignored. There should only be one "1.0 release". Make is expected to be 3.
		if(major == 0)
			return "The Main Release"+(build>0?" (Dev Build "+build+')':"");
		
		return (make==1?"Pre-Alpha ":make==2?"Alpha ":make==3?"Beta ":"Update ")+major+(minor<0?" (Final)":"."+minor)+(build>0?" (Dev Build "+build+')':"");
	}
}
