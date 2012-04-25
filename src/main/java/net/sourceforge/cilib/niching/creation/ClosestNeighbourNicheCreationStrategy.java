/**
 * Computational Intelligence Library (CIlib)
 * Copyright (C) 2003 - 2010
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science
 * University of Pretoria
 * South Africa
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.cilib.niching.creation;

import java.util.Arrays;
import net.sourceforge.cilib.algorithm.population.PopulationBasedAlgorithm;
import net.sourceforge.cilib.controlparameter.ConstantControlParameter;
import net.sourceforge.cilib.controlparameter.LinearlyVaryingControlParameter;
import net.sourceforge.cilib.controlparameter.UpdateOnIterationControlParameter;
import net.sourceforge.cilib.entity.Entity;
import net.sourceforge.cilib.entity.Particle;
import net.sourceforge.cilib.entity.Topology;
import net.sourceforge.cilib.entity.visitor.ClosestEntityVisitor;
import net.sourceforge.cilib.math.random.generator.MersenneTwister;
import net.sourceforge.cilib.measurement.generic.Iterations;
import net.sourceforge.cilib.niching.NichingSwarms;
import net.sourceforge.cilib.niching.merging.SingleSwarmMergeStrategy;
import net.sourceforge.cilib.problem.boundaryconstraint.ClampingBoundaryConstraint;
import net.sourceforge.cilib.pso.PSO;
import net.sourceforge.cilib.pso.iterationstrategies.SynchronousIterationStrategy;
import net.sourceforge.cilib.pso.particle.ParticleBehavior;
import net.sourceforge.cilib.pso.velocityprovider.ClampingVelocityProvider;
import net.sourceforge.cilib.pso.velocityprovider.GCVelocityProvider;
import net.sourceforge.cilib.pso.velocityprovider.StandardVelocityProvider;
import net.sourceforge.cilib.stoppingcondition.Maximum;
import net.sourceforge.cilib.stoppingcondition.MeasuredStoppingCondition;

/**
 * <p>
 * Create a set of niching locations, based on a provided set of identified
 * niching entities.
 * </p>
 * <p>
 * For each newly discovered niching location, a new sub-swarmType is creates that will
 * maintain the niche. For the case of the PSO, the niching particle and the closest
 * particle to the identified particle are gropuped into a niche. Sub-swarms will always
 * then have at least two particles.
 * </p>
 * <p>
 * The rational for two particles is that a particle is a social entity and as a result
 * needs to share information. Ensuring that there are at least two particles within
 * a sub-swarmType will enable the velocity update equation associated with the particle
 * to still operate.
 * </p>
 */
public class ClosestNeighbourNicheCreationStrategy extends NicheCreationStrategy {
    
    /**
     * Default constructor.
     */
    public ClosestNeighbourNicheCreationStrategy() {
        this.swarmType = new PSO();
        ((SynchronousIterationStrategy) ((PSO) this.swarmType).getIterationStrategy()).setBoundaryConstraint(new ClampingBoundaryConstraint());
        this.swarmType.addStoppingCondition(new MeasuredStoppingCondition(new Iterations(), new Maximum(), 500));

        ClampingVelocityProvider delegate = new ClampingVelocityProvider(ConstantControlParameter.of(1.0),
                new StandardVelocityProvider(new UpdateOnIterationControlParameter(new LinearlyVaryingControlParameter(0.7, 0.2)),
                    ConstantControlParameter.of(1.2), ConstantControlParameter.of(1.2), new MersenneTwister(), new MersenneTwister()));
        
        GCVelocityProvider gcVelocityProvider = new GCVelocityProvider();
        gcVelocityProvider.setDelegate(delegate);
        gcVelocityProvider.setRho(ConstantControlParameter.of(0.01));
        
        this.swarmBehavior = new ParticleBehavior();
        this.swarmBehavior.setVelocityProvider(gcVelocityProvider);
    }

    @Override
    public NichingSwarms f(NichingSwarms a, Entity b) {
        //There should be at least two particles
        if (a._1().getTopology().size() <= 1) {
            return a;
        }

        ClosestEntityVisitor closestEntityVisitor = new ClosestEntityVisitor();
        closestEntityVisitor.setTargetEntity(b);
        a._1().accept(closestEntityVisitor);
        
        Particle nicheMainParticle = (Particle) b.getClone();
        Particle nicheClosestParticle = (Particle) closestEntityVisitor.getResult().getClone();
        
        nicheMainParticle.setNeighbourhoodBest(nicheMainParticle);
        nicheClosestParticle.setNeighbourhoodBest(nicheMainParticle);
        
        nicheMainParticle.setParticleBehavior(swarmBehavior.getClone());
        nicheClosestParticle.setParticleBehavior(swarmBehavior.getClone());
        
        PopulationBasedAlgorithm newSubSwarm = swarmType.getClone();
        newSubSwarm.setOptimisationProblem(a._1().getOptimisationProblem());
        newSubSwarm.getTopology().clear();
        ((Topology<Particle>) newSubSwarm.getTopology()).addAll(Arrays.asList(nicheMainParticle, nicheClosestParticle));
        
        PopulationBasedAlgorithm newMainSwarm = a._1().getClone();
        newMainSwarm.getTopology().clear();
        for(Entity e : a._1().getTopology()) {
            if (!e.equals(b) && !e.equals(closestEntityVisitor.getResult())) {
                ((Topology<Entity>) newMainSwarm.getTopology()).add(e.getClone());
            }
        }
        
        return NichingSwarms.of(new SingleSwarmMergeStrategy().f(newMainSwarm, null), a._2().cons(newSubSwarm));
    }
}