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
package net.sourceforge.cilib.functions.continuous;

import net.sourceforge.cilib.functions.ContinuousFunction;
import net.sourceforge.cilib.type.types.container.Vector;

/**
 * SchwefelProblem1_2.
 *
 * Characteristics:
 *
 * <li>Unimodal</li>
 * <li>Non Separable</li>
 *
 * f(x) = 0; x = (0,0,...,0)
 *
 * x e [-100,100]
 *
 * R(-100,100)^30
 * <p>
 * Implementation: 
 * http://web.mysites.ntu.edu.sg/epnsugan/PublicSite/Shared%20Documents/Forms/AllItems.aspx?RootFolder=%2fepnsugan%2fPublicSite%2fShared%20Documents%2fCEC2005&FolderCTID=&View=%7bDAF31868%2d97D8%2d4779%2dAE49%2d9CEC4DC3F310%7
 * </p>
 *
 */
public class SchwefelProblem1_2 implements ContinuousFunction {

    private static final long serialVersionUID = -65519037071861168L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Double apply(Vector input) {
        double prevSum;
        double currSum = 0.0;
        double outerSum = 0.0;

        for (int i = 0; i < input.size(); i ++) {
            prevSum = currSum;
            currSum = prevSum + input.doubleValueOf(i);
            outerSum += currSum * currSum;
        }

        return outerSum;
    }
}
