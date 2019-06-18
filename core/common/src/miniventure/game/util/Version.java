package miniventure.game.util;

import org.jetbrains.annotations.NotNull;

public class Version implements Comparable<Version> {
	
	static class VersionFormatException extends IllegalArgumentException {
		public VersionFormatException(String versionString) {
			super("The string \"" + versionString + "\" does not represent a valid version format; valid formats are #.# (deprecated) or #.#.# or #.#.#.dev");
		}
	}
	
	private final int make, major, minor;
	private final boolean dev;
	
	private Version() {make = 0; major = 0; minor = 0; dev = false;}
	public Version(String version) {
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
	
	// returns a string representation in the same form as what is expected in the constructor.
	public String serialize() {
		StringBuilder str = new StringBuilder(12);
		str.append(make).append('.').append(major);
		if(minor >= 0) {
			str.append('.').append(minor);
			if(dev)
				str.append(".dev");
		}
		return str.toString();
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
		if(dev != other.dev) return dev ? -1 : 1; // the dev version comes before the non-dev one.
		
		return 0; // versions are the same.
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Version)) return false;
		Version o = (Version) other;
		return make==o.make && major==o.major && minor==o.minor && dev==o.dev;
	}
	
	@Override
	public int hashCode() {
		int result = make;
		result = 31 * result + major;
		result = 31 * result + minor;
		result = 17 * result + (dev ? 1 : 0);
		return result;
	}
	
	@Override
	public String toString() {
		// note: when major is 0, minor is ignored. There should only be one "1.0 release". Make is expected to be 3.
		if(major == 0)
			return "The Main Release"+(dev?" (Dev Build)":"");
		
		return (make==1?"Pre-Alpha ":make==2?"Alpha ":make==3?"Beta ":"Update ")+major+(minor<0?" (Final)":"."+minor)+(dev?" (Dev Build)":"");
	}
}
