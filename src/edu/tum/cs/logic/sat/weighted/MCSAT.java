/*
 * Created on Aug 7, 2009
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.tum.cs.logic.sat.weighted;

import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;

import edu.tum.cs.logic.GroundAtom;
import edu.tum.cs.logic.PossibleWorld;
import edu.tum.cs.logic.WorldVariables;
import edu.tum.cs.logic.sat.SampleSAT;
import edu.tum.cs.srl.Database;

public class MCSAT {

	protected WeightedClausalKB kb;
	protected WorldVariables vars;
	protected Database db; 
	protected Random rand;
	protected SampledDistribution dist;
	protected double p = 0.9;
	protected final boolean verbose = true;
	
	public MCSAT(WeightedClausalKB kb, WorldVariables vars, Database db) {		
		this.kb = kb;
		this.vars = vars;
		this.db = db;
		this.rand = new Random();
		this.dist = new SampledDistribution(vars);
	}

	public void run(int steps) throws Exception {
		if(verbose) {
			System.out.println("MC-SAT constraints:");
			for(WeightedClause wc : kb)
				System.out.println("  " + wc);
		}
		
		// find initial state satisfying all hard constraints
		Vector<WeightedClause> M = new Vector<WeightedClause>();
		for(Entry<WeightedFormula, Vector<WeightedClause>> e : kb.getFormulasAndClauses()) {
			WeightedFormula wf = e.getKey();
			if(wf.isHard) {
				M.addAll(e.getValue());
			}
		}
		PossibleWorld state = new PossibleWorld(vars);
		SampleSAT sat = new SampleSAT(M, state, vars, db);
		sat.setP(p);
		sat.run();
		
		// actual MC-SAT sampling
		for(int i = 0; i < steps; i++) {
			M.clear();
			for(Entry<WeightedFormula, Vector<WeightedClause>> e : kb.getFormulasAndClauses()) {
				WeightedFormula wf = e.getKey();
				if(wf.formula.isTrue(sat.getState())){
					boolean satisfy = wf.isHard || rand.nextDouble() * Math.exp(wf.weight) > 1.0;
					if(satisfy)
						M.addAll(e.getValue());
				}
			}
			if(verbose || (i+1) % 100 == 0) {
				System.out.printf("  step %d: %d constraints to be satisfied\n", i+1, M.size());
				if(verbose) {
					for(WeightedClause wc : M)
						System.out.println("    " + wc);
				}
			}
			sat.initConstraints(M);
			sat.run();
			
			if(true) {
				sat.getState().print();
			}
			
			dist.addSample(sat.getState(), 1.0);
		}
		dist.normalize();
	}
	
	public void setP(double p) {
		this.p = p;
	}
	
	public double getResult(GroundAtom ga) {
		return dist.getResult(ga.index);
	}
	
	public class SampledDistribution{
		public double[] sums;
		public double Z;
		
		public SampledDistribution(WorldVariables vars){
			this.Z = 0.0;
			this.sums = new double[vars.size()];
		}
		
		public void addSample(PossibleWorld w, double weight){
			for(GroundAtom ga : w.getVariables()){
				if(w.isTrue(ga)){
					sums[ga.index] += weight;
				}
			}
			Z += weight;
		}
		
		public void normalize(){
			for(int i = 0; i < sums.length; i++){
				sums[i] /= Z;
			}
			Z = 1.0;
		}
		
		public double getResult(int indx){
			return sums[indx];
		}
	}
	
}
