package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.tool.IdlToSchemataTool;

/** * Converts an entire directory from Avro IDL (.avdl) to schema (.avsc) */
public class ConvertIdl {
	public static void main(String[] args) throws Exception {
		IdlToSchemataTool tool = new IdlToSchemataTool();
		File inDir = new File(args[0]);
		File outDir = new File(args[1]);
		for (File inFile : inDir.listFiles()) {
			List<String> toolArgs = new ArrayList<String>();
			toolArgs.add(inFile.getAbsolutePath());
			toolArgs.add(outDir.getAbsolutePath());
			tool.run(System.in, System.out, System.err, toolArgs);
		}
	}
}
