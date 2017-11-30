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
	
	private static final int CHUNK_SIZE = 64; // perlin noise is individually generated for each chunk.
	
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
	
	private float[][] generateChunkNoise(int chunkX, int chunkY) {
		
		float[][] chunkNoise = new float[CHUNK_SIZE][CHUNK_SIZE];
		//int radius = 4;
		
		int worldX = chunkX * CHUNK_SIZE;
		int worldY = chunkY * CHUNK_SIZE;
		
		/*worldX -= radius;
		worldY -= radius;
		
		float[][][][] smoothNoise = new float[CHUNK_SIZE][CHUNK_SIZE][radius*2][radius*2];
		
		for(int x = 0; x < CHUNK_SIZE; x++) {
			for(int y = 0; y < CHUNK_SIZE; y++) {
				smoothNoise[x][y] = PerlinNoiseGenerator.generateSmoothNoise(hashFunction, worldX+x, worldY+y, radius*2, radius*2, 1);
				*//*float average = 0;
				for(float[] row: perlinSmoothed)
					for(float val: row)
						average += val;
				*//*
				//average /= radius*radius*4;
				//perlinSmoothed = PerlinNoiseGenerator.generateSmoothNoise()
				//chunkNoise[x][y] = perlinSmoothed[radius][radius];
			}
		}*/
		
		for(int x = 0; x < CHUNK_SIZE; x++) {
			for(int y = 0; y < CHUNK_SIZE; y++) {
				chunkNoise[x][y] = getWorldValue(worldX+x, worldY+y);
			}
		}
		
		//chunkNoise = PerlinNoiseGenerator.generateSmoothNoise(hashFunction, chunkX*CHUNK_SIZE, chunkY*CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, 1);
		
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
	
	private float getWorldValue(int worldX, int worldY) {
		int radius = 2;
		
		float[][][][] smoothed = new float[radius*2+1][radius*2+1][][];
		
		for(int x = -radius; x <= radius; x++) {
			for(int y = -radius; y <= radius; y++) {
				// get a 2D array of the surrounding tiles, smoothed.
				// for each tile in the radius, get a 2D array surrounding it, smoothed.
				smoothed[x+radius][y+radius] = PerlinNoiseGenerator.generateSmoothNoise(hashFunction, x+worldX, y+worldY, radius*2+1, radius*2+1, 2);
			}
		}
		
		float average = 0;
		//float totalWeight = 0;
		float maxDist = (float) Math.sqrt(2*radius*radius);
		float origValue = smoothed[radius][radius][0][0];
		
		for(int x = -radius; x <= radius; x++) {
			for(int y = -radius; y <= radius; y++) {
				// return the value that is the weighted average of all the values for the given tile (closer to this tile has a higher weight)
				float dist = (float) Math.sqrt(x*x + y*y);//Math.min(Math.abs(x), Math.abs(y));
				float weight = PerlinNoiseGenerator.map(dist*dist, 0, maxDist*maxDist, 0, 1);
				//float weight = dist == 0 ? 1.5f : (1.0f / dist);
				float val = smoothed[x+radius][y+radius][x+radius][y+radius];
				val = PerlinNoiseGenerator.interpolate(origValue, val, weight);
				
				//totalWeight += weight;
				average += val;
			}
		}
		
		//average /= totalWeight;
		average /= Math.pow(radius*2+1, 2);
		
		return average;
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
			int scale = 8;
			int width = CHUNK_SIZE, height = CHUNK_SIZE;
			
			LevelGen gen = new LevelGen(new Random().nextLong(), width, height);
			
			//float[][] totalMap = PerlinNoiseGenerator.generatePerlinNoise(gen.sampleSpace, 5);
			//totalMap = PerlinNoiseGenerator.generateSmoothNoise(totalMap, 2);
			
			float[][] totalMap = gen.generateChunkNoise(0, 0);/*new float[width][height];
			
			float[][] topRight = gen.generateChunkNoise(1, 1);
			float[][] topLeft = gen.generateChunkNoise(0, 1);
			float[][] bottomLeft = gen.generateChunkNoise(0, 0);
			float[][] bottomRight = gen.generateChunkNoise(1, 0);
			
			for(int y = 0; y < height/2; y++) {
				System.arraycopy(topRight[y], 0, totalMap[y], width/2, height/2);
				System.arraycopy(topLeft[y], 0, totalMap[y], 0, height/2);
				System.arraycopy(bottomLeft[y], 0, totalMap[y+height/2], 0, height/2);
				System.arraycopy(bottomRight[y], 0, totalMap[y+height/2], height/2, height/2);
			}*/
			
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
			
			float minVal = totalMap[0][0];
			float maxVal = totalMap[0][0];
			for (int x = 0; x < totalMap.length; x++) {
				for (int y = 0; y < totalMap[x].length; y++) {
					float val = totalMap[x][y]; // should be a value between 0 and 1
					minVal = Math.min(minVal, val);
					maxVal = Math.max(maxVal, val);
					int col = (int) (val * 255);
					g.setColor(new Color(col, col, col));
					//g.setColor(terrainColors.get(getTerrain(val)));
					g.fillRect(x * scale, y * scale, scale, scale);
				}
			}
			
			System.out.println("minimum value: " + minVal);
			System.out.println("maximum value: " + maxVal);
			
			for (int x = 0; x < totalMap.length; x++)
				for (int y = 0; y < totalMap[x].length; y++)
					totalMap[x][y] = PerlinNoiseGenerator.map(totalMap[x][y], minVal, maxVal, 0, 1);
			
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
