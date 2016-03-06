import cspSolver.BTSolver;
import cspSolver.BTSolver.ConsistencyCheck;
import cspSolver.BTSolver.ValueSelectionHeuristic;
import cspSolver.BTSolver.VariableSelectionHeuristic;
import sudoku.SudokuBoardGenerator;
import sudoku.SudokuBoardReader;
import sudoku.SudokuFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class monsterSudokuSolver
{
	public static void main(String[] args) throws FileNotFoundException
	{
		long startTime = System.currentTimeMillis();
		Scanner inFile = new Scanner(new File(args[0]));
		PrintWriter outFile = new PrintWriter(args[1]);
		long currentTime, totalStart, ppStart, ppDone, searchStart, searchDone, solutionTime;
		totalStart = 0;
		//
		// THIS WILL CHANGE WHEN WE IMPLEMENT AC
		//
		ppStart = 0;
		ppDone = 0;
		//
		BTSolver solver;
		
		// GEN option means generate board
		
		if (args.length > 3 && args[3].equals("GEN")) {
			int m = inFile.nextInt();
			int N = inFile.nextInt();
			int p = inFile.nextInt();
			int q = inFile.nextInt();
			if (N != p * q || m > N * N || m < 0 || N < 0 || p < 0 || q < 0) {
				outFile.print("error: invalid input parameters");
			} else {
				SudokuFile sf = SudokuBoardGenerator.generateBoard(N, p, q, m, Long.parseLong(args[2]));
				if (sf == null) {
					outFile.print("timeout");
				} else {
					outFile.print(sf.writeToFile());
				}
			}
			
		// Solve an existing board	
			
		} else {
			currentTime = System.currentTimeMillis();
			searchStart = currentTime - startTime;
			solver = new BTSolver(SudokuBoardReader.readFile(args[0]), Long.parseLong(args[2]));
			solver.setConsistencyChecks(ConsistencyCheck.None);
			solver.setValueSelectionHeuristic(ValueSelectionHeuristic.None);
			solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.None);
			
			if (Arrays.asList(args).contains("FC")) solver.setConsistencyChecks(ConsistencyCheck.ForwardChecking);
			if (Arrays.asList(args).contains("MRV")) solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.MinimumRemainingValue);
			if (Arrays.asList(args).contains("DH")) solver.setVariableSelectionHeuristic(VariableSelectionHeuristic.Degree);
			if (Arrays.asList(args).contains("LCV")) solver.setValueSelectionHeuristic(ValueSelectionHeuristic.LeastConstrainingValue);
			
			Thread t1 = new Thread(solver);
			try
			{
				t1.start();
				t1.join(60000);
				if(t1.isAlive())
				{
					t1.interrupt();
				}
			}catch(InterruptedException e)
			{
			}
	
			currentTime = System.currentTimeMillis();
			searchDone = currentTime - startTime;
			
//			if(solver.hasSolution())
//			{
//				solver.printSolverStats();
//				System.out.println(solver.getSolution());
//				outFile.print(solver.getSolution());	
//			} 
			
			currentTime = System.currentTimeMillis();
			solutionTime = currentTime - startTime;
						
			// write to output file
			outFile.println("TOTAL_START=" + totalStart);
			outFile.println("PREPROCESSING_START=" + ppStart);
			outFile.println("PREPROCESSING_DONE=" + ppDone);
			outFile.println("SEARCH_START=" + searchStart/1000.0);
			outFile.println("SEARCH_DONE=" + searchDone/1000.0);
			outFile.println("SOLUTION_TIME=" + solutionTime/1000.0);
			if (solver.hasSolution()) {
				outFile.println("STATUS=success");
				outFile.println("SOLUTION=(" + solver.getSolution().printAssignments() + ")");
			} else if (solver.timedOut() || (int)solutionTime == (int)Long.parseLong(args[2])) {
				outFile.println("STATUS=timeout");
				outFile.println("SOLUTION=(" + solver.getSolution().printEmpty() + ")");
			} else {
				outFile.println("STATUS=error");
				outFile.println("SOLUTION=(" + solver.getSolution().printEmpty() + ")");
			}
			outFile.println("COUNT_NODES=" + solver.getNumAssignments());
			outFile.println("COUNT_DEADENDS=" + solver.getNumBacktracks());
	
		}
		inFile.close();
		outFile.close();
		
	}
}
