/* ------------------------------------------------------------

 Problem Description
 -------------------

 In the classical Job-Shop Scheduling problem a finite set of jobs is
 processed on a finite set of machines. Each job is characterized by a
 fixed order of operations, each of which is to be processed on a
 specific machine for a specified duration.  Each machine can process
 at most one operation at a time and once an operation initiates
 processing on a given machine it must complete processing
 uninterrupted.  The objective of the problem is to find a schedule
 that minimizes the makespan of the schedule.

 ------------------------------------------------------------ */

import ilog.concert.*;
import ilog.cp.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SchedJobShop_TWT {

	static class DataReader {

		private StreamTokenizer st;

		public DataReader(String filename) throws IOException {
			FileInputStream fstream = new FileInputStream(filename);
			Reader r = new BufferedReader(new InputStreamReader(fstream));
			st = new StreamTokenizer(r);
		}

		public int next() throws IOException {
			st.nextToken();
			return (int) st.nval;
		}
	}

	static class IntervalVarList extends ArrayList<IloIntervalVar> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IloIntervalVar[] toArray() {
			return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this
					.size()]);
		}
	}

	static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
		return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
	}

	public static void main(String[] args) throws IOException {

		String filename = "C:/benchmark/abz3.data";
		int failLimit = 100000;
		int nbJobs, nbMachines;

		if (args.length > 0)
			filename = args[0];
		if (args.length > 1)
			failLimit = Integer.parseInt(args[1]);

		IloCP cp = new IloCP();
		DataReader data = new DataReader(filename);
		try {
			nbJobs = data.next();
			nbMachines = data.next();
			List<IloIntExpr> ends = new ArrayList<IloIntExpr>();
			IntervalVarList[] machines = new IntervalVarList[nbMachines];
			for (int j = 0; j < nbMachines; j++)
				machines[j] = new IntervalVarList();
			IloIntervalVar[][] itv = new IloIntervalVar[nbJobs][nbMachines];

			double[][] p = new double[nbJobs][nbMachines];
			double[] due = new double[nbJobs];
			double factor = 1.5d;
			int[] w = { 4, 4, 2, 2, 2, 2, 2, 2, 1, 1 };
			
			for (int i = 0; i < nbJobs; i++) {
				double pp = 0;
				for (int j = 0; j < nbMachines; j++) {
					pp = p[i][j] + pp;
				}
				due[i] = (int) Math.floor(factor * pp);
			}

			for (int i = 0; i < nbJobs; i++) {
				IloIntervalVar prec = cp.intervalVar();
				for (int j = 0; j < nbMachines; j++) {
					int m, d;
					m = data.next();
					d = data.next();

					p[i][j] = d;

					IloIntervalVar ti = cp.intervalVar(d);
					machines[m].add(ti);
					if (j > 0) {
						cp.add(cp.endBeforeStart(prec, ti));
					}
					prec = ti;
					 itv[i][j]=prec;
				}
				ends.add(cp.endOf(prec));
			}



			for (int j = 0; j < nbMachines; j++)
				cp.add(cp.noOverlap(machines[j].toArray()));

			IloIntExpr[] aa = arrayFromList(ends);
			IloNumExpr zz = cp.intExpr();

			for (int i = 0; i < nbJobs; i++) {
				zz = cp.sum(zz,
						cp.prod(w[i], cp.max(cp.sum(aa[i], -due[i]), 0)));

			}

			IloObjective objective = cp.minimize(zz);
			cp.add(objective);

			cp.setParameter(IloCP.IntParam.FailLimit, failLimit);
			System.out.println("Instance \t: " + filename);
			// IloIntVar x = cp.intVar(0, 10);
			if (cp.solve()) {
				System.out.println("Makespan \t: " + cp.getObjValue());
					for (int i = 0; i < nbJobs; i++) {
						System.out.print("{");
						for (int j = 0; j < nbMachines; j++) {
							
							System.out.print(" " + cp.getStart(itv[i][j])+", ");
						}
						System.out.println("},");
					}
			} else {
				System.out.println("No solution found.");
			}
		} catch (IloException e) {
			System.err.println("Error: " + e);
		}
	}
}
