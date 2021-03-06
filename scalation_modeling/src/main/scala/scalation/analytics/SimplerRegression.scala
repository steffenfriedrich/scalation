
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.4
 *  @date    Fri Jan  5 14:03:36 EST 2018
 *  @see     LICENSE (MIT style license file).
 */

package scalation.analytics

import scalation.linalgebra.{MatriD, MatrixD, VectoD, VectorD}
import scalation.math.double_exp
import scalation.plot.Plot
import scalation.util.Error

//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `SimplerRegression` class supports simpler linear regression.
 *  In this case,  the vector 'x' consists of a single variable 'x0'.
 *  Fit the parameter vector 'b' in the regression equation
 *  <p>
 *      y  =  b dot x + e  =  [b0] dot [x0] + e  =  b0 * x0 + e
 *  <p>
 *  where 'e' represents the residuals (the part not explained by the model).
 *  The simpler regression model has no intercept parameter, only a slope parameter.
 *  @see `SimpleRegression` for both intercept and slope parameters
 *  @param x  the input/design matrix
 *  @param y  the response vector
 */
class SimplerRegression (x: MatriD, y: VectoD)
      extends Predictor with Error
{
    if (x.dim2 != 1)     flaw ("constructor", "design matrix must have 1 columns")
    if (x.dim1 != y.dim) flaw ("constructor", "dimensions of x and y are incompatible")

    private val k        = 1                              // number of variables
    private val m        = x.dim1.toDouble                // number of data points (rows)
    private val r_df     = (m-1.0) / (m-2.0)              // ratio of degrees of freedom

    private var rBarSq   = -1.0                           // adjusted R-squared
    private var fStat    = -1.0                           // F statistic (quality of fit)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Train the predictor by fitting the parameter vector (b-vector) in the
     *  simpler regression equation
     *  <p>
     *      y = b dot x + e  = b0 * x0 + e
     *  <p>
     *  using the least squares method.
     *  @param yy  the response vector
     */
    def train (yy: VectoD = y): SimplerRegression =
    {
        val x0  = x.col(0)                                // get column 0 of x = [x0]
        val ssx = x0 dot x0                               // sum of squares x0
        val sxy = x0 dot y                                // sum of cross products x0, y

        b = new VectorD (1)                               // parameter vector [b0]
        b(0) = sxy / ssx                                  // slope

        this
    } // train

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Train the predictor by fitting the parameter vector (b-vector) in the
     *  simple regression equation for the response passed into the class 'y'.
     */
//    def train (): SimplerRegression = train (y)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the error and useful diagnostics.
     *  @param yy   the response vector
     */
    def eval (yy: VectoD = y)
    {
        e = yy - x * b                                         // compute residual/error vector e
        diagnose (yy)                                          // compute diagnostics
    } // eval


    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute diagostics for the regression model.
     *  @param yy  the response vector
     */
    override protected def diagnose (yy: VectoD)
    {
        super.diagnose (yy)
        rBarSq = 1.0 - (1.0-rSq) * r_df                   // R-bar-squared (adjusted R-squared)
        fStat  = (sst - sse) * (m-2.0) / sse              // F statistic (msr / mse)
    } // diagnose

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the quality of fit including.
     */
    override def fit: VectoD = super.fit.asInstanceOf [VectorD] ++ VectorD (rBarSq, fStat)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the labels for the fit.
     */
    override def fitLabels: Seq [String] = super.fitLabels ++ Seq ("rBarSq", "fStat")

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Predict the value of 'y = f(z)' by evaluating the formula 'y = b dot z',
     *  i.e., '[b0] dot [z0]'.
     *  @param z  the new vector to predict
     */
    def predict (z: VectoD): Double = b dot z

} // SimplerRegression class


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `SimplerRegression` companion object provides a simple factory method
 *  for building simple regression linear regression models.
 */
object SimplerRegression
{
    //:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create a Simpler Linear Regression model, automatically creating a
     *  a design/data matrix from the vector 'x'.
     *  @param x  the input/design m-by-1 vector
     *  @param y  the response m-vector
     */
    def apply (x: VectoD, y: VectoD): SimplerRegression =
    {
        val xx = new MatrixD (x.dim, 1)
        xx.setCol (0, x)
        new SimplerRegression (xx, y)
    } // apply

} // SimplerRegression object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `SimplerRegressionTest` object is used to test the `SimplerRegression` class.
 *  <p>
 *      y = b0 * x + e
 *  <p>
 *  > runMain scalation.analytics.SimplerRegressionTest
 */
object SimplerRegressionTest extends App
{
    // 4 data points:
    val x = VectorD (1, 2, 3, 4)
    val y = VectorD (1, 3, 3, 4)
//  val y = VectorD (1, 3, 2, 4)

    println ("x = " + x)
    println ("y = " + y)

    val rg = SimplerRegression (x, y)
    rg.train ().eval ()

    println ("coefficient = " + rg.coefficient)
    println ("            = " + rg.fitLabels)
    println ("fit         = " + rg.fit)

} // SimplerRegressionTest object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `SimplerRegressionTest2` object is used to test the `SimplerRegression` class.
 *  <p>
 *      y = b dot x + e = [b0] dot [x0] + e
 *  <p>
 *  > runMain scalation.analytics.SimplerRegressionTest2
 */
object SimplerRegressionTest2 extends App
{
    // 5 data points:            x0
    val x = new MatrixD ((5, 1), 0.0,               // x 5-by-1 matrix
                                 1.0,
                                 2.0,
                                 3.0,
                                 4.0)
    val y = VectorD (2.0, 3.0, 5.0, 4.0, 6.0)       // y vector

    println ("x = " + x)
    println ("y = " + y)

    val rg = new SimplerRegression (x, y)
    rg.train ().eval ()

    println ("coefficient = " + rg.coefficient)
    println ("            = " + rg.fitLabels)
    println ("fit         = " + rg.fit)

    val z  = VectorD (5.0)                                             // predict y for one point
    val yp = rg.predict (z)
    println ("predict (" + z + ") = " + yp)

    val yyp = VectorD (for (i <- x.range1) yield rg.predict (x(i)))    // predict y for several points
    println ("predict (" + x + ") = " + yyp)

    new Plot (x.col(0), y, yyp)

} // SimplerRegressionTest2 object


//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `SimplerRegressionTest3` object is used to test the `SimplerRegression` class.
 *  <p>
 *      y = b dot x = b0 * x0
 *  <p>
 *  @see http://mathbits.com/mathbits/tisection/Statistics2/linear.htm
 *  > runMain scalation.analytics.SimplerRegressionTest3
 */
object SimplerRegressionTest3 extends App
{
    // 20 data points: just x0 coordinate
    val x0 = VectorD (  4.0,   9.0,  10.0,  14.0,   4.0,   7.0,  12.0,  22.0,   1.0,   3.0,
                        8.0,  11.0,   5.0,   6.0,  10.0,  11.0,  16.0,  13.0,  13.0,  10.0)
    val y  = VectorD (390.0, 580.0, 650.0, 730.0, 410.0, 530.0, 600.0, 790.0, 350.0, 400.0,
                      590.0, 640.0, 450.0, 520.0, 690.0, 690.0, 770.0, 700.0, 730.0, 640.0)

    println ("x0 = " + x0)
    println ("y  = " + y)

    val rg = SimplerRegression (x0, y)
    rg.train ().eval ()

    println ("coefficient = " + rg.coefficient)
    println ("            = " + rg.fitLabels)
    println ("fit         = " + rg.fit)

    val z  = VectorD (15.0)                                            // predict y for one point
    val yp = rg.predict (z)
    println ("predict (" + z + ") = " + yp)

    val yyp = VectorD (for (i <- x0.range) yield rg.predict (VectorD (x0(i))))    // predict y for several points
    println ("predict (" + x0 + ") = " + yyp)
    
    new Plot (x0, y, yyp)

} // SimplerRegressionTest3 object

