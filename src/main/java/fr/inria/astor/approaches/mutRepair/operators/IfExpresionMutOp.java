package fr.inria.astor.approaches.mutRepair.operators;

import java.util.ArrayList;
import java.util.List;

import fr.inria.astor.core.entities.ModificationInstance;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.SuspiciousModificationPoint;
import fr.inria.astor.core.loop.evolutionary.spaces.operators.AstorOperator;
import fr.inria.astor.core.loop.mutation.mutants.core.MutantCtElement;
import fr.inria.astor.core.loop.mutation.mutants.operators.LogicalBinaryOperatorMutator;
import fr.inria.astor.core.loop.mutation.mutants.operators.MutatorComposite;
import fr.inria.astor.core.loop.mutation.mutants.operators.NegationUnaryOperatorConditionMutator;
import fr.inria.astor.core.loop.mutation.mutants.operators.RelationalBinaryOperatorMutator;
import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;

public class IfExpresionMutOp extends AstorOperator {

	MutatorComposite mutatorBinary = null;

	public IfExpresionMutOp() {
		super();
		this.mutatorBinary = new MutatorComposite(MutationSupporter.getFactory());
		this.mutatorBinary.getMutators().add(new RelationalBinaryOperatorMutator(MutationSupporter.getFactory()));
		this.mutatorBinary.getMutators().add(new LogicalBinaryOperatorMutator(MutationSupporter.getFactory()));
		this.mutatorBinary.getMutators().add(new NegationUnaryOperatorConditionMutator(MutationSupporter.getFactory()));

	}

	@Override
	public boolean applyChangesInModel(ModificationInstance operation, ProgramVariant p) {
		boolean successful = false;
		CtExpression rightTerm = null, leftTerm = null;
		try {

			CtExpression ctst = (CtExpression) operation.getOriginal();
			CtExpression fix = (CtExpression) operation.getModified();

			ctst.replace((CtExpression) fix);
			successful = true;
			operation.setSuccessfulyApplied((successful));

			log.debug(" applied: " + ctst.getParent().getSignature());

		} catch (Exception ex) {
			log.error("Error applying an operation, exception: " + ex.getMessage());
			operation.setExceptionAtApplied(ex);
			operation.setSuccessfulyApplied(false);
		}
		return true;
	}

	@Override
	public boolean applyToPoint(ModificationPoint point) {
		return (point.getCodeElement() instanceof CtIf);
	}

	@Override
	public boolean needIngredient() {
		return false;
	}

	protected ModificationInstance createModificationInstance(ModificationPoint point, MutantCtElement fix)
			throws IllegalAccessException {
		CtIf targetIF = (CtIf) point.getCodeElement();
		ModificationInstance operation = new ModificationInstance();
		operation.setOriginal(targetIF.getCondition());
		operation.setOperationApplied(this);
		operation.setModificationPoint(point);
		operation.setModified(fix.getElement());

		return operation;
	}

	@Override
	public List<ModificationInstance> createModificationInstance(SuspiciousModificationPoint modificationPoint) {
		List<ModificationInstance> ops = new ArrayList<>();

		CtIf targetIF = (CtIf) modificationPoint.getCodeElement();

		List<MutantCtElement> mutations = getMutants(targetIF);

		for (MutantCtElement mutantCtElement : mutations) {
			ModificationInstance opInstance;
			try {
				opInstance = createModificationInstance(modificationPoint, mutantCtElement);
				if (opInstance != null)
					ops.add(opInstance);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
		return ops;
	}

	/** Return the list of CtElements Mutanted */
	public List<MutantCtElement> getMutants(CtIf targetIF) {
		List<MutantCtElement> mutations = null;
		mutations = this.mutatorBinary.execute(targetIF.getCondition());
		return mutations;
	}

	@Override
	public boolean undoChangesInModel(ModificationInstance opInstance, ProgramVariant p) {
		try {
			CtExpression ctst = (CtExpression) opInstance.getOriginal();
			CtExpression fix = (CtExpression) opInstance.getModified();
			fix.replace(ctst);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean updateProgramVariant(ModificationInstance opInstance, ProgramVariant p) {
		// TODO Auto-generated method stub
		return false;
	}

}
