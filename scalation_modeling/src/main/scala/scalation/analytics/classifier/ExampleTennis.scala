
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  John Miller
 *  @version 1.4
 *  @date    Fri Feb 16 16:14:34 EST 2018
 *  @see     LICENSE (MIT style license file).
 */

package scalation.analytics.classifier

import scalation.linalgebra.MatrixI

//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `ExampleTennis` object is used to test all integer based classifiers.
 *  This is the well-known classification problem on whether to play tennis
 *  based on given weather conditions.  Applications may need to slice 'xy'.
 *  <p>
 *      val x = xy.sliceCol (0, 4)      // columns 0, 1, 2, 3
 *      val y = xy.col (4)              // column 4
 *  <p>
 *  @see euclid.nmu.edu/~mkowalcz/cs495f09/slides/lesson004.pdf
 */
object ExampleTennis
{
    // combined data matrix [ x | y ]
    // dataset ----------------------------------------------------------------
    // x0: Outlook:     Rain (0),   Overcast (1), Sunny (2)
    // x1: Temperature: Cold (0),   Mild (1),     Hot (2)
    // x2: Humidity:    Normal (0), High (1)
    // x3: Wind:        Weak (0),   Strong (1)
    // y:  the response/classification decision
    // variables/features:          x0     x1     x2     x3    y
    val xy = new MatrixI ((14, 5),  2,     2,     1,     0,    0,      // day  1
                                    2,     2,     1,     1,    0,      // day  2
                                    1,     2,     1,     0,    1,      // day  3
                                    0,     1,     1,     0,    1,      // day  4
                                    0,     0,     0,     0,    1,      // day  5
                                    0,     0,     0,     1,    0,      // day  6
                                    1,     0,     0,     1,    1,      // day  7
                                    2,     1,     1,     0,    0,      // day  8
                                    2,     0,     0,     0,    1,      // day  9
                                    0,     1,     0,     0,    1,      // day 10
                                    2,     1,     0,     1,    1,      // day 11
                                    1,     1,     1,     1,    1,      // day 12
                                    1,     2,     0,     0,    1,      // day 13
                                    0,     1,     1,     1,    0)      // day 14

    val fn = Array ("Outlook", "Temp", "Humidity", "Wind")             // feature names
    val cn = Array ("No", "Yes")                                       // class names for y
    val k  = cn.size                                                   // number of classes

} // ExampleTennis object

