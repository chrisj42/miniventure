package miniventure.game.world;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Random;

import miniventure.game.world.tile.TileType;

import net.openhft.hashing.LongHashFunction;

public class LevelGen {
	
	private static final int CHUNK_SIZE = 16; // perlin noise is individually generated for each chunk.
	
	private final long worldSeed;
	private final LongHashFunction hashFunction;
	
	public LevelGen(long worldSeed, int width, int height) {
		this.worldSeed = worldSeed;
		//Random random = new Random(worldSeed);
		//sampleSpace = PerlinNoiseGenerator.generateWhiteNoise(worldSeed, width, height);
		// generate noise sample set
		hashFunction = LongHashFunction.xx(worldSeed);
	}
	
	private TileType[] generateChunk(int worldX, int worldY) {
		return null;
	}
	
	private float[][] generateChunkNoise(int worldX, int worldY) {
		
		float[][] chunkNoise = new float[CHUNK_SIZE][CHUNK_SIZE];
		int radius = 8;
		
		worldX -= radius/2;
		worldY -= radius/2;
		
		for(int y = 0; y < chunkNoise.length; y++) {
			for(int x = 0; x < chunkNoise[y].length; x++) {
				float[][] perlinSmoothed = PerlinNoiseGenerator.generatePerlinNoise(hashFunction, worldX+x, worldY+y, radius, radius, 8);
				//perlinSmoothed = PerlinNoiseGenerator.generateSmoothNoise()
				chunkNoise[y][x] = perlinSmoothed[radius/2][radius/2];
			}
		}
		
		return chunkNoise;
		
		//long seed = Long.parseUnsignedLong(Integer.toBinaryString(worldX)+Integer.toBinaryString(worldY), 2);
		
		//System.out.println("seed = " + seed);
		
		/*
			The sample space isn't infinite, I just have to deal with that.
			But... perhaps I could rearrange the sample space based on a random algorithm, seeded with the coordinates?
			Then again, how to correlate chunks... perhaps, generate chunks in between other chunks, and somehow use that to generate the final terrain..?
			
			The key point is:
				- I need to be able to generate a specific chunk using only the coordinates and world seed. I don't mind having to generate other chunks along with it, but I obviously can't generate *everything*.
			
			Perhaps the coordinates picks the filters to apply..? no...
			
			I need to find a way to use coordinates as they are; they should directly correlate to the result.
		 */
		
		//Random seedGen = new Random();
		//seedGen.setSeed(this.seed);
		
		//float[][] whiteNoise
		//float[][] perlin = PerlinNoiseGenerator.generatePerlinNoise(sampleSpace, 6);
		//System.out.println("generated " + Arrays.deepToString(perlin));
		//perlin = PerlinNoiseGenerator.generateSmoothNoise(perlin, 0);
		//return sampleSpace;
	}
	
	private static HashMap<String, Color> terrainColors = new HashMap<String, Color>() {{
		put("water", Color.blue);
		put("rock", Color.gray);
		put("sand", Color.yellow);
		put("grass", Color.green);
		put("tree", Color.green.darker().darker());
		
	}};
	
	public static void main(String[] args) {
		while(true) {
			int scale = 16;
			int width = CHUNK_SIZE*2, height = CHUNK_SIZE*2;
			
			LevelGen gen = new LevelGen(new Random().nextLong(), width, height);
			
			//float[][] totalMap = PerlinNoiseGenerator.generatePerlinNoise(gen.sampleSpace, 5);
			//totalMap = PerlinNoiseGenerator.generateSmoothNoise(totalMap, 2);
			
			float[][] totalMap = new float[CHUNK_SIZE*2][CHUNK_SIZE*2];
			
			float[][] topRight = gen.generateChunkNoise(0, 0);
			float[][] topLeft = gen.generateChunkNoise(-CHUNK_SIZE*2, 0);
			float[][] bottomLeft = gen.generateChunkNoise(-CHUNK_SIZE*2, -CHUNK_SIZE*2);
			float[][] bottomRight = gen.generateChunkNoise(0, -CHUNK_SIZE*2);
			
			for(int y = 0; y < CHUNK_SIZE; y++) {
				System.arraycopy(topRight[y], 0, totalMap[y], CHUNK_SIZE, CHUNK_SIZE);
				System.arraycopy(topLeft[y], 0, totalMap[y], 0, CHUNK_SIZE);
				System.arraycopy(bottomLeft[y], 0, totalMap[y+CHUNK_SIZE], 0, CHUNK_SIZE);
				System.arraycopy(bottomRight[y], 0, totalMap[y+CHUNK_SIZE], CHUNK_SIZE, CHUNK_SIZE);
			}
			
			/*
				- fetch coordinates to generate terrain for
				- use coordinates to seed random number generator
				- use seeded random number generator to generate "random" values for base noise, for that region.
			 */
			/*float inc = 1.0f/(width*height);
			
			float[][] totalMap = new float[width][height];
			for(int i = 0; i < width*height; i++) {
				Random random = new Random(i);
				float val = random.nextFloat();
				//System.out.println(i+"="+val);
				totalMap[i%width][i/width] = val;
				//if(random.nextLong() > Long.MAX_VALUE/2)
					//random.nextLong();
				*//*for(int j = 0; j < totalMap[i].length; j++) {
					random = new Random(random.nextLong());
					totalMap[i][j] = random.nextInt(width)*1.0f/width;//((random.nextInt()*1.0f/Integer.MAX_VALUE) - (i == 0 ? 0 : totalMap[i - 1][j] - 1));
					float val = totalMap[i][j];
					if(val < 0 || val > 1) System.out.println(val);
				}*//*
			}*/
			
			/*float[][] newMap = new float[width][height];
			for(int i = 1; i < width*height; i++) {
				float cVal = totalMap[i%width][i/width];
				float pVal = totalMap[(i-1)%width][(i-1)/width];
				float diffVal = Math.abs(cVal-pVal);
				newMap[i%width][i/width] = diffVal;
				if(diffVal > 0.5) System.out.println(diffVal);
			}
			totalMap = newMap;*/
			
			BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			
			for (int x = 0; x < totalMap.length; x++) {
				for (int y = 0; y < totalMap[x].length; y++) {
					float val = totalMap[x][y]; // should be a value between 0 and 1
					//int col = (int) (val * 255);
					//g.setColor(new Color(col, col, col));
					g.setColor(terrainColors.get(getTerrain(val)));
					g.fillRect(x * scale, y * scale, scale, scale);
				}
			}
			
			JPanel viewPanel = new JPanel() {
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(image.getWidth(), image.getHeight());
				}
				
				@Override
				protected void paintComponent(Graphics g) {
					g.drawImage(image, 0, 0, null);
				}
			};
			
			JOptionPane.showMessageDialog(null, viewPanel, "Level", JOptionPane.PLAIN_MESSAGE);
			
			//break;
		}
	}
	
	private static String getTerrain(float val) {
		val = val * val;
		if(val < 0.3f) return "water";
		else if(val < 0.35f) return "sand";
		else if(val < 0.5f) return "grass";
		else if(val < 0.58f) return "tree";
		else if(val < 0.75f) return "grass";
		else return "rock";
	}
}
