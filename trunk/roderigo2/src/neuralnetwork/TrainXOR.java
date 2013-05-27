package neuralnetwork;

public class TrainXOR {
	// Inputs for xor problem
	static final double inputs[][] = {{1, 1}, {1, 0}, {0, 1}, {0, 0}};

	// Corresponding outputs, xor training data
	static final double expectedOutputs[][] = {{0}, {1}, {1}, {0}};
	
	public static void main(String[] args) {
		NeuralNetwork nn = new NeuralNetwork(2, 2, 1);
		nn.train(inputs, expectedOutputs, 0.8, 0.0, 0.001, 50000);
		
		for(int i = 0; i < inputs.length; i++) {
			StringBuilder line = new StringBuilder("input:");
			for(int j = 0; j < inputs[i].length; j++) {
				line.append(" ").append(inputs[i][j]);
			}
			line.append("  target:");
			for(int j = 0; j < expectedOutputs[i].length; j++) {
				line.append(" ").append(expectedOutputs[i][j]);
			}
			nn.setInput(inputs[i]);
			nn.feedForward();
			double out[] = nn.getOutput();
			line.append("  out:");
			for(int j = 0; j < out.length; j++) {
				line.append(" ").append(out[j]);
			}
			System.out.println(line);
		}
	}
}
