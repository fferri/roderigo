package roderigo;

import gnu.prolog.term.*;
import gnu.prolog.vm.Environment;
import gnu.prolog.vm.Interpreter;
import gnu.prolog.vm.PrologCode;
import gnu.prolog.vm.Interpreter.Goal;
import gnu.prolog.vm.PrologException;

public class PrologTest {
	public static void main(String[] args) {
		Environment env = new Environment();
		Interpreter interp = env.createInterpreter();
		VariableTerm L = new VariableTerm("L");
		CompoundTerm t = new CompoundTerm(AtomTerm.get("member"), new Term[] {
			L,
			new CompoundTerm(AtomTerm.get("."), new Term[] {
				new IntegerTerm(1),
				new CompoundTerm(AtomTerm.get("."), new Term[] {
					new IntegerTerm(2),
					new CompoundTerm(AtomTerm.get("."), new Term[] {
						new IntegerTerm(3),
						AtomTerm.get("[]")
					})
				})
			})
		});
		
		try {
			// one shot:
			/*interp.runOnce(t);
			System.out.println(L.name + " = " + L.value);*/
			
			// multiple:
			Goal g = interp.prepareGoal(t);
			int rc;
			// if you stop running interp.execute(goal) where rc is still SUCCESS, then you need to run interp.stop(goal)
			while((rc = interp.execute(g)) == PrologCode.SUCCESS) {
				System.out.println(L.name + " = " + L.value);
			}

		} catch(PrologException ex) {
			ex.printStackTrace();
		}
	}
}
