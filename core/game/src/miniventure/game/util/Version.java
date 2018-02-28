package miniventure.game.util;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
	
	static class VersionFormatException extends IllegalArgumentException {
		public VersionFormatException(String versionString) {
			super("The string \"" + versionString + "\" does not represent a valid version format; valid formats are #.# or #.#.#");
		}
	}
	
	private int make, major, minor;
	
	public Version(String version) {
		String[] nums = version.split("\\.");
		if(nums.length > 3 || nums.length < 2)
			throw new VersionFormatException(version);
		
		try {
			make = new Integer(nums[0]);
			major = new Integer(nums[1]);
			if(nums.length == 3)
				minor = new Integer(nums[2]);
			else
				minor = -1;
			
			if(make < 0 || major < 0 || (nums.length == 3 && minor < 0))
				throw new VersionFormatException(version);
			
		} catch(NumberFormatException ex) {
			throw new VersionFormatException(version);
		}
	}
	
	@Override
	public int compareTo(@NotNull Version other) {
		if(make != other.make) return Integer.compare(make, other.make);
		if(major != other.major) return Integer.compare(major, other.major);
		if(minor != other.minor) {
			if(minor < 0) return 1; // -1 means after all other numbers, so this is after the other version.
			else if(other.minor < 0) return -1; // this is before the other version.
			else return Integer.compare(minor, other.minor); // compare normally.
		}
		
		return 0; // versions are the same.
	}
	
	@Override
	public String toString() {
		return make+"."+major+(minor<0?" official":", pre-release "+minor);
	}
}
