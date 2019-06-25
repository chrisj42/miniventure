package miniventure.game.util;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
	
	static class VersionFormatException extends IllegalArgumentException {
		public VersionFormatException(String versionString) {
			super("The string \"" + versionString + "\" does not represent a valid version format; valid formats are #.#.# or #.#.#.#");
			// deprecated formats are #.# and #.#.#.dev
		}
	}
	
	private final int make, major, minor, build;
	
	private Version() {make = 0; major = 0; minor = 0; build = 0;}
	public Version(String version) {
		String[] nums = version.split("\\.");
		if(nums.length == 2 || version.contains("dev")) {
			OldVersion v = new OldVersion(version);
			make = v.make;
			major = v.major;
			minor = v.minor;
			build = v.dev ? 1 : 0;
			return;
		}
		
		if(nums.length < 3 || nums.length > 4)
			throw new VersionFormatException(version);
		
		try {
			make = new Integer(nums[0]);
			major = new Integer(nums[1]);
			minor = new Integer(nums[2]);
			if(nums.length == 3)
				build = 0; // release build
			else
				build = new Integer(nums[3]);
			
		} catch(NumberFormatException ex) {
			throw new VersionFormatException(version);
		}
	}
	
	// returns a string representation in the same form as what is expected in the constructor.
	public String serialize() {
		return String.valueOf(make) + '.' + major + '.' + minor + '.' + build;
	}
	
	public boolean isBefore(String version) { return isBefore(new Version(version)); }
	public boolean isBefore(@NotNull Version version) {
		return compareTo(version) < 0;
	}
	public boolean atOrBefore(String version) { return atOrBefore(new Version(version)); }
	public boolean atOrBefore(@NotNull Version version) {
		return compareTo(version) <= 0;
	}
	
	public boolean isAfter(String version) { return isAfter(new Version(version)); }
	public boolean isAfter(@NotNull Version version) {
		return compareTo(version) > 0;
	}
	public boolean atOrAfter(String version) { return atOrAfter(new Version(version)); }
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
	
	private static class OldVersion {
		private final int make, major, minor;
		private final boolean dev;
		
		OldVersion(String version) {
			String[] nums = version.split("\\.");
			if(nums.length > 4 || nums.length < 2)
				throw new VersionFormatException(version);
			
			try {
				make = new Integer(nums[0]);
				major = new Integer(nums[1]);
				if(nums.length > 2)
					minor = new Integer(nums[2]);
				else
					minor = -1; // this is only supported for legacy purposes
				
				if(make < 0 || major < 0 || (nums.length > 2 && minor < 0))
					throw new VersionFormatException(version);
				
				dev = nums.length > 3;
				if(dev && !nums[3].equalsIgnoreCase("dev"))
					throw new VersionFormatException(version);
				
			} catch(NumberFormatException ex) {
				throw new VersionFormatException(version);
			}
		}
	}
}
