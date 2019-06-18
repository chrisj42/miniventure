package miniventure.game.world.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import miniventure.game.GameCore;
import miniventure.game.util.Version;

public class WorldReference {
	
	public final Version version; // todo when reading this in world select screen, check if it is a dev build, and display a compatibility warning if so.
	public final Path folder;
	public final String worldName;
	public final long timestamp;
	
	public final boolean compatible;
	public final String[] dataErrors;
	public final String[] dataWarnings;
	// public final int extraFiles;
	
	public WorldReference(Path folder) throws IOException {
		// validate the folder
		this.folder = folder;
		worldName = folder.getFileName().toString();
		version = WorldFileInterface.getWorldVersion(folder);
		timestamp = WorldFileInterface.getTimestamp(folder);
		compatible = GameCore.isCompatible(version);
		
		Set<Path> children = Files.list(folder).collect(Collectors.toCollection(HashSet::new));
		
		Collection<String> missingFiles = WorldFileInterface.validateWorldFiles(children);
		
		int extraFiles = children.size();
		if(extraFiles > 0)
			dataWarnings = new String[] {extraFiles+" extra files detected in world folder."};
		else
			dataWarnings = new String[0];
		
		dataErrors = new String[missingFiles.size()];
		int i = 0;
		for(String file: missingFiles)
			dataErrors[i++] = "Missing file '"+file+"'!";
	}
	
	public boolean isValid() {
		return dataErrors.length == 0;
	}
	
	@Override
	public String toString() {
		return worldName;
	}
	
	/*private static void testFor(Path worldFolder, String file) throws FileNotFoundException {
		if(!Files.exists(worldFolder.resolve(file)))
			throw new FileNotFoundException(file);
	}*/
	
	public static LinkedList<WorldReference> getLocalWorlds(boolean includeInvalid) {
		
		Path saveDir = WorldFileInterface.getLocation("");
		LinkedList<WorldReference> worlds = new LinkedList<>();
		try {
			Files.walkFileTree(saveDir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if(dir.equals(saveDir)) return FileVisitResult.CONTINUE;
					// GameCore.debug("trying "+dir);
					try {
						WorldReference ref = new WorldReference(dir);
						if(includeInvalid || ref.isValid())
							worlds.add(ref);
						// GameCore.debug("world found: "+dir);
					} catch(Exception ignored) {}
					return FileVisitResult.SKIP_SUBTREE;
				}
			});
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// sort the worlds from newest to oldest before returning
		worlds.sort(Comparator.comparing(ref -> ref.timestamp, (t1, t2) -> Long.compare(t2, t1)));
		return worlds;
	}
}
