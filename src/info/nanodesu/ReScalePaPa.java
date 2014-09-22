package info.nanodesu;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ReScalePaPa {
	
	public static void main(String[] args) throws IOException {
		ReScalePaPa s = new ReScalePaPa(.5f);
		s.readFile(new File("E:\\Games\\PA\\Planetary Annihilation\\stable\\media\\pa\\units\\land\\bot_factory\\bot_factory.papa"));
		s.process();
		s.writeOutput(new File("E:\\Games\\PA\\Planetary Annihilation\\stable\\media\\pa\\units\\land\\bot_factory\\bot_factory.papa"));
	}
	
	public enum VertexFormat {
		Position3Normal3Color4TexCoord4(7, 11*4), Position3Weights4bBones4bNormal3TexCoord2(8, 10*4), Position3Normal3Tan3Bin3TexCoord4(10, 16*4);
		
		private int key;
		private int vertexSize;
		
		private VertexFormat(int key, int vertexSize) {
			this.key = key;
			this.vertexSize = vertexSize;
		}
		
		public static VertexFormat fromKey(int k) {
			for (VertexFormat f: VertexFormat.values()) {
				if (f.key == k) {
					return f;
				}
			}
			throw new RuntimeException("unknown key for vertex format: "+k);
		}
		
		@Override
		public String toString() {
			return super.name() + "["+key+"]";
		}
		
		public int getVertexSize() {
			return vertexSize;
		}
	}
	
	private float factor;
	
	private byte[] bytes;
	
	public ReScalePaPa(float f) {
		factor = f;
	}
	
	private long readNBytes(int offset, int n) {
		long result = 0;
		for (int i = n-1; i >= 0; i--) {
			result |= ((bytes[offset+i] & 0xFF) << (i * 8));
		}
		return result;
	}
	
	// I read int64 as int32, as I am very doubtful int64 is actually ever required (and handling int64 array coordinates isn't easy to do)
	private int readInt(int offset) {
		return (int) readNBytes(offset, 4);
	}
	
	private int readShort(int offset) {
		return (int) readNBytes(offset, 2);
	}
	
	private void writeInt(int offset, int value) {
		for (int i = 0; i < 4; i++) {
			byte bits = (byte) (value >> (i * 8));
			bytes[offset + i] = bits;
		}
	}
	
	private float readFloat(int offset) {
		return Float.intBitsToFloat(readInt(offset));
	}
	
	private void writeFloat(int offset, float value) {
		int bits = Float.floatToIntBits(value);
		writeInt(offset, bits);
	}
	
	public void readFile(File file) throws IOException {
		bytes = FileUtils.readFileToByteArray(file);
	}
	
	public void process() {
		processVertices();
		processBones();
	}

	public void writeOutput(File file) throws IOException {
		FileUtils.writeByteArrayToFile(file, bytes);
	}
	
	private void processVertices() {
		int numberOfMeshes = getNumberOfMeshes();
		int verticesStart = findVerticesStructOffset();
		
		for (int m = 0; m < numberOfMeshes; m++) {
			int readBase = verticesStart + 24 * m;
			VertexFormat vertexFormat = VertexFormat.fromKey(readInt(readBase));
			int numberOfVertices = readInt(readBase + 4);
			int verticesBlockOffset = readInt(readBase + 16);
			
			for (int i = 0; i < numberOfVertices; i++) {
				for (int j = 0; j < 3; j++) {
					int position = verticesBlockOffset + j * 4 + i * vertexFormat.vertexSize;
					float base = readFloat(position);
					float scaled = base * factor;
					writeFloat(position, scaled);
				}
			}
		}
	}
	
	private void processBones() {
		int skeletonLocation = findSkeletonHeaderOffset();
		int numerOfBones = readInt(skeletonLocation);
		int bonesLocation = readInt(skeletonLocation + 8);
		
		int skeletonSegmentSize = 132;
		
		for (int i = 0; i < numerOfBones; i++) {
			int translationsOffset = 4;
			int boneOffset = 116;
			
			for (int j = 0; j < 3; j++) {
				int positionBase = bonesLocation + j * 4 + skeletonSegmentSize * i;
				int tpos = positionBase + translationsOffset;
				int bonOff = positionBase + boneOffset;
				
				writeFloat(tpos, factor * readFloat(tpos));
				writeFloat(bonOff, factor * readFloat(bonOff)); // no idea what this offset is for exactly, but it looks way better if I dont touch it (?)
			}
		}
	}
	
	private int findVerticesStructOffset() {
		return (int) readInt(48);
	}
	
	private int getNumberOfMeshes() {
		return readShort(18);
	}
	
	private int findSkeletonHeaderOffset() {
		return (int) readInt(80);
	}
}
