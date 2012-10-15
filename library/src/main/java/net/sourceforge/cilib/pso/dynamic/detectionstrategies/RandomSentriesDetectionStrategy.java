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
package net.sourceforge.cilib.pso.dynamic.detectionstrategies;

import fj.F;
import fj.data.List;
import net.sourceforge.cilib.algorithm.Algorithm;
import net.sourceforge.cilib.algorithm.population.PopulationBasedAlgorithm;
import net.sourceforge.cilib.controlparameter.ConstantControlParameter;
import net.sourceforge.cilib.controlparameter.ControlParameter;
import net.sourceforge.cilib.entity.Entity;
import net.sourceforge.cilib.util.selection.Samples;
import net.sourceforge.cilib.util.selection.recipes.RandomSelector;

/**
 * This class defines a detection strategy that uses a user-specified
 * {@link #numberOfSentries number of sentry entities} and an {@local #epsilon} value to
 * detect whether a change has occurred in the environment.
 *
 * @TechReport{ title = "Tracking Changing Extrema with Particle Swarm Optimizer", author =
 *              "Anthony Jack Carlisle and Gerry V. Dozier", institution = "Huntingdon
 *              College", year = "2001", number = "CSSE01-08" }
 * @param <E> some {@link PopulationBasedAlgorithm population based algorithm}
 */
public class RandomSentriesDetectionStrategy<E extends PopulationBasedAlgorithm> extends EnvironmentChangeDetectionStrategy<E> {
    private static final long serialVersionUID = -7299802900616282412L;

    protected ControlParameter numberOfSentries;

    public RandomSentriesDetectionStrategy() {
        numberOfSentries = ConstantControlParameter.of(1.0);
    }

    public RandomSentriesDetectionStrategy(RandomSentriesDetectionStrategy<E> rhs) {
        super(rhs);
        numberOfSentries = rhs.numberOfSentries.getClone();
    }

    @Override
    public RandomSentriesDetectionStrategy<E> getClone() {
        return new RandomSentriesDetectionStrategy<E>(this);
    }

    /**
     * After every {@link #interval} iterations, pick
     * {@link #numberOfSentries a number of} random entities from the given
     * {@link Algorithm algorithm's} topology and compare their previous fitness values with
     * their current fitness values. An environment change is detected when the difference
     * between the previous and current fitness values are &gt;= the specified
     * {@link #epsilon} value.
     *
     * @param algorithm used to get hold of topology of entities and number of iterations
     * @return true if a change has been detected, false otherwise
     */
    @Override
    public boolean detect(E algorithm) {
        if (algorithm.getIterations() % interval == 0) {
            return List.<Entity>iterableList(new RandomSelector().on(algorithm.getTopology())
                .select(Samples.first((int) numberOfSentries.getParameter())))
                .exists(new F<Entity, Boolean>() {
                    @Override
                    public Boolean f(Entity sentry) {
                        double previousFitness = sentry.getFitness().getValue();
                        sentry.calculateFitness();
                        double currentFitness = sentry.getFitness().getValue();

                        return Math.abs(previousFitness - currentFitness) >= epsilon;
                    }
                });
        }
        return false;
    }

    public void setNumberOfSentries(ControlParameter parameter) {
        if (parameter.getParameter() <= 0) {
            throw new IllegalArgumentException("It doesn't make sense to have <= 0 sentry points");
        }

        numberOfSentries = parameter;
    }

    public ControlParameter getNumberOfSentries() {
        return numberOfSentries;
    }
}