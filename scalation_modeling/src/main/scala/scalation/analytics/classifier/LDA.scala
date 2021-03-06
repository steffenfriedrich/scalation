
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.4
 *  @date    Sat Jan  9 21:48:57 EST 2016
 *  @see     LICENSE (MIT style license file).
 */

package scalation.analytics.classifier

import scala.math.log

import scalation.linalgebra.{MatriD, MatrixD, VectoD, VectorD, VectoI, VectorI}
import scalation.plot.Plot
import scalation.util.banner

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `LDA` class implements a Linear Discriminant Analysis 'LDA' classifier.
 *  It places a vector into a group according to its maximal discriminant function.
 *  @see en.wikipedia.org/wiki/Linear_discriminant_analysis
 *  @param x   the real-valued training/test data vectors stored as rows of a matrix
 *  @param y   the training/test classification vector, where y_i = class for row i of the matrix x
 *  @param fn  the names for all features/variables
 *  @param cn  the names for all classes
 */
class LDA (x: MatrixD, y: VectoI, fn: Array [String], cn: Array [String] = Array ("no", "yes"))
      extends ClassifierReal (x, y, fn, 2, cn)
{
    private val DEBUG = true                                                           // debug flag
    private val x1 = (MatrixD (for (i <- 0 until x.dim1 if y(i) == 0) yield x(i))).t   // group 1
    private val x2 = (MatrixD (for (i <- 0 until x.dim1 if y(i) == 1) yield x(i))).t   // group 2

    if (DEBUG) {
        banner ("grouped matrices")
        println ("x1 = " + x1)
        println ("x2 = " + x2)
//      new Plot (x1.col(0), x1.col(1), null, "matrix x1")
//      new Plot (x2.col(0), x2.col(1), null, "matrix x2")
    } // if

    private val mu  = (x1.mean, x2.mean, x.mean)                   // means
    private val xc0 = x1 - mu._3                                   // corrected group matrix 1
    private val xc1 = x2 - mu._3                                   // corrected group matrix 2

    private var pcovar: MatriD  = null                             // pooled covariance matrix
    private var ipcovar: MatriD = null                             // inverse of pooled covariance matrix
    private var prior: VectoD   = null                             // prior probabilities

    if (DEBUG) {
        banner ("corrected grouped matrices")
        println ("xc0   = " + xc0)
        println ("xc1   = " + xc1)
        banner ("their mean vectors and covariance matrices")
        println ("mu    = " + mu)
    } // if

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the corrected covariance matrix.
     *  @param xc  the corrected martix whose corrected covariance matrix is sought 
     */
    def corrected_cov (xc: MatriD): MatriD = (xc.t * xc) / xc.dim1

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Train the classifer by computing 'pcovar', 'ipcovar' and 'prior'
     *  that are needed to compute the discriminant functions 'f'.
     *  These are computed in the 'classify' method.
     *  @param itest  the indices of the test data - FIX - not used yet
     */
    def train (itest: IndexedSeq [Int]): LDA =
    {
        val w1    = x1.dim1 / x.dim1.toDouble                      // first weigth
        val w2    = 1.0 - w1                                       // second weigth
        val covar = (corrected_cov (xc0), corrected_cov (xc1))     // corrected covariances
        prior     = VectorD (w1, w2)                               // prior probabilities
        pcovar    = covar._1 * w1 + covar._2 * w2                  // pooled covariance matrix
        ipcovar   = pcovar.inverse                                 // inverse of pooled covariance matrix   
        if (DEBUG) {
            banner ("pooled covariance and it inverse")
            println ("covar   = " + covar)
            println ("pcovar  = " + pcovar)
            println ("ipcovar = " + ipcovar)
            println ("prior   = " + prior)
        } // if
        this
    } // train

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Reset method not applicable.
     */
    def reset (): Unit = ???

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Classify vector 'z' by computing its discriminant function 'f' for each
     *  group and return the group index with the maximun value for 'f'.
     */
    override def classify (z: VectoD): (Int, String, Double) =
    {
        val fvec = (ipcovar * mu._1, ipcovar * mu._2)
        val f    = ((fvec._1 dot z) - 0.5 * (fvec._1 dot mu._1) + log (prior(0)),
                    (fvec._2 dot z) - 0.5 * (fvec._2 dot mu._2) + log (prior(1)))
        if (DEBUG) println ("f = " + f)
        if (f._1 > f._2) (0, cn(0), 0.0) else (1, cn(1), 1.0)
    } // classify

} // LDA class


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `LDATest` is used to test the `LDA` class.
 *  @see people.revoledu.com/kardi/tutorial/LDA/Numerical%20Example.html
 *  > runMain scalation.analytics.classifier.LDATest
 */
object LDATest extends App
{
    // features/variable: 
    // x1: curvature
    // x2: diameter
    //                           x1    x2
    val x = new MatrixD ((7, 2), 2.95, 6.63,
                                 2.53, 7.79,
                                 3.57, 5.65,
                                 3.16, 5.47,
                                 2.58, 4.46,
                                 2.16, 6.22,
                                 3.27, 3.52)
    val y = VectorI (0, 0, 0, 0, 1, 1, 1)

    val fn = Array ("curvature", "diameter")                   // feature names
    val cn = Array ("pass", "fail")                            // class names
    val cl = new LDA (x, y, fn, cn)                            // create the LDA classifier
    cl.train ()

    banner ("classify")
    val z  = VectorD (2.81, 5.46)
    println (s"classify ($z) = ${cl.classify (z)}")

} // LDATestObject

