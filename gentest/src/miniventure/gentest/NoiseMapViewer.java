package miniventure.gentest;

import javax.swing.JLabel;

import miniventure.game.world.levelgen.GroupNoiseMapper;
import miniventure.game.world.levelgen.NoiseMultiplexer;

import org.jetbrains.annotations.NotNull;

public class NoiseMapViewer extends NoisePanel {
	
	@NotNull private final NoiseMultiplexer map;
	
	public NoiseMapViewer(@NotNull NoiseMultiplexer map) {
		// TODO make this and make editors go in diff tabs and merge gentest into core, but keep package path
		// NOTE I wanted to make noise function editors, map editors, and region editors off of templates, but they are all so different... at least in enough ways. Maybe I could but it would probably not be much in the framework...
		
		this.map = map;
		
		add(new JLabel("Type: "+(map instanceof GroupNoiseMapper ? "Island generator" : "Multiplexer")));
		add(new JLabel("Source function: "+map.getSource().getName()));
		add(new JLabel("Regions: "+))
		
		/*
			also remem this:

- add back ability to select any many dispersed list items as you want and reorder them

- implement edit button to make new editor tab that grays out the other tabs and the regen world button until it is closed by either confirm or cancel

- allow custom colors for region tiletypes (and specify tiletype colors separately instead of in tiletypeenum class?)


noise function panel:
	- name
	- noise coords
	- curve count

maps:
	- name
	- parameters
		- noise function
		- color fetcher
		- float (size)
	- source function
	- regions
		- size (>=0 float)
		- color fetcher (own component)

		 */
	}
	
	@Override
	public void setObjectName(@NotNull String name) {
		
	}
	
	@NotNull
	@Override
	public String getObjectName() {
		return null;
	}
}
