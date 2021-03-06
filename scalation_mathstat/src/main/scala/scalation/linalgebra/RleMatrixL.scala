
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** @author  Vishnu Gowda Harish, Vinay Kumar Bingi, John Miller
 *  @version 1.4
 *  @date    Sat Oct 1 1:10:11 EDT 2016
 *  @see     LICENSE (MIT style license file).
 */

package scalation.linalgebra

import java.io.PrintWriter

import scala.io.Source.fromFile
import scala.math.{abs => ABS, max => MAX, min => MIN}

import scalation.math.{long_exp, oneIf}
import scalation.math.ExtremeD.TOL
import scalation.util.{Error, ReArray}

import RleMatrixL._

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `RleMatrixL` class stores and operates on Numeric Matrices of type `Long`.
 *  Rather than storing the matrix as a 2 dimensional array, it is stored as an array
 *  of RleVectorL's.
 *  @param d1  the first/row dimension
 *  @param d2  the second/column dimension
 *  @param v   the 1D array used to store matrix elements
 */
 class RleMatrixL (val d1: Int, 
                   val d2: Int, 
                   private var v: Array [RleVectorL] = null,
                   val deferred: Boolean = false)
        extends MatriL with Error with Serializable
{ 
    /** Dimension 1
     */
    lazy val dim1 = d1

    /** Dimension 2
     */
    lazy val dim2 = d2
    
    if (v == null) {
        v = Array.ofDim [RleVectorL] (d2)
        for (i <- 0 until v.length) {
            val v1 = new ReArray [TripletL] (1)
            v1(0) = new TripletL (0l, d1, 0)
            v(i) = new RleVectorL (d1, v1)
        } // for   
    } // if

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Construct a 'dim1' by 'dim1' square matrix.
     *  @param dim1  the row and column dimension
     */
    def this (dim1: Int) { this (dim1, dim1) }
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an exact copy of 'this' m-by-n matrix.
     */
    def copy (): RleMatrixL = new RleMatrixL (dim1, dim2, v.clone())

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an m-by-n matrix with all elements intialized to zero.
     *  @param m  the number of rows
     *  @param n  the number of columns
     */
    def zero (mm: Int, nn: Int): RleMatrixL = RleMatrixL (new MatrixL (mm, nn))
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get 'this' matrix's element at the 'i, j'-th index position. 
     *  @param i  the row index
     *  @param j  the column index
     */
    def apply (i: Int, j: Int): Long = v(j)(i)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get 'this' matrix's vector at the 'i'-th index position ('i'-th row).
     *  @param i  the row index
     */
    def apply (i: Int): RleVectorL = { RleVectorL (for (j <- range2) yield  v(j)(i)) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get a slice 'this' matrix row-wise on range 'ir' and column-wise on range 'jr'.
     *  Ex: b = a(2..4, 3..5)
     *  @param ir  the row range
     *  @param jr  the column range
     */
    def apply (ir: Range, jr: Range): MatriL = slice (ir.start, ir.end, jr.start, jr.end)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the underlying 1D array for 'this' matrix.
     */
    def apply (): Array [RleVectorL] = v
   
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's element at the 'i, j'-th index position to the scalar 'x'.
     *  @param i  the row index
     *  @param j  the column index
     *  @param x  the scalar value to assign
     */
    def update (i: Int, j: Int, x: Long) { v(j)(i) = x }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's row at the 'i'-th index position to the vector 'u'.
     *  @param i  the row index
     *  @param u  the vector value to assign
     */
    def update (i: Int, u: VectoL) { for (j <- range2)  v(j)(i) = u(j) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set a slice 'this' matrix row-wise on range ir and column-wise on range 'jr'.
     *  Ex: a(2..4, 3..5) = b
     *  @param ir  the row range
     *  @param jr  the column range
     *  @param b   the matrix to assign
     */ 
    def update (ir: Range, jr: Range, b: MatriL)
    {
        if (b.isInstanceOf [RleMatrixL]) {
            val bb = b.asInstanceOf [RleMatrixL]
            for (i <- ir; j <- jr) this(i, j) = bb(i, j)
        } else {
            flaw ("update","must convert b to RleMatrixL first")
        } // if
    } // update
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set all the elements in 'this' matrix to the scalar 'x'.
     *  @param x  the scalar value to assign
     */
    def set (x: Long) { for (i <- range1; j <- range2) v(i)(j) = x }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set 'this' matrix's 'i'-th row starting at column 'j' to the vector 'u'.
     *  @param i  the row index
     *  @param u  the vector value to assign
     *  @param j  the starting column index
     */
    def set (i: Int, u: VectoL, j: Int = 0) { for (k <- 0 until u.dim) v(i)(k+j) = u(k) }
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set all the values in 'this' matrix as copies of the values in 2D array 'u'.
     *  @param u  the 2D array of values to assign
     */
    def set (u: Array[Array[Long]]) 
    { 
        v = (for (j <- range2) yield RleVectorL (for (i <- range1) yield u(j)(i))).toArray
    } // set
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' `RleMatrixL` into a `MatrixI`.
     */
    def toInt: MatrixI =
    {
        val c = new MatrixI (dim1, dim2)
        for (i <- range1) c(i) = this(i).toInt
        c
    } // toInt
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' matrix to a dense matrix.
     */
    def toDense: MatrixL = 
    {
        val c = new MatrixL (dim1, dim2)
        for (i <- range1) c(i) = this(i)
        c
    } // toDense

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix row-wise 'from' to 'end'.
     *  @param from  the start row of the slice (inclusive)
     *  @param end   the end row of the slice (exclusive)
     */
    def slice (from: Int, end: Int): RleMatrixL =
    { 
        RleMatrixL (for (i <- range2) yield RleVectorL (for (j <- from until end) yield v(i)(j)))       
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix column-wise 'from' to 'end'.
     *  @param from  the start column of the slice (inclusive)
     *  @param end   the end column of the slice (exclusive)
     */
    def sliceCol (from: Int, end: Int): RleMatrixL = 
    {
        if (from >= end) return new RleMatrixL (0, 0)
        RleMatrixL (for (i <- from until end) yield v(i))
    } // sliceCol

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix row-wise 'r_from' to 'r_end' and column-wise 'c_from' to 'c_end'.
     *  @param r_from  the start of the row slice
     *  @param r_end   the end of the row slice
     *  @param c_from  the start of the column slice
     *  @param c_end   the end of the column slice
     */
    def slice (r_from: Int, r_end: Int, c_from: Int, c_end: Int): RleMatrixL = 
    {       
        RleMatrixL (for (i <- c_from until c_end) yield RleVectorL (for (j <- r_from until r_end) yield v(i)(j)))
    } // slice

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Slice 'this' matrix excluding the given row and/or column.
     *  @param row  the row to exclude
     *  @param col  the column to exclude
     */
    def sliceExclude (row: Int, col: Int): RleMatrixL =
    {
        RleMatrixL (for (i <- range2 if i != col) yield RleVectorL (for (j <- range1 if j != row) yield v(i)(j))) 
    } // sliceExclude

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select rows from 'this' matrix according to the given index/basis.
     *  @param rowIndex  the row index positions (e.g., (0, 2, 5))
     */
    def selectRows (rowIndex: Array [Int]): RleMatrixL =
    { 
        val c = new RleMatrixL (rowIndex.length, dim2)
        for (i <- c.range1) c(i) = this (rowIndex (i))
        c
    } // selectRows

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get column 'col' from the matrix, returning it as a vector.
     *  @param col   the column to extract from the matrix
     */
    def col (col: Int): RleVectorL =  v(col)

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get column 'col' from the matrix, returning it as a vector.
     *  @param col   the column to extract from the matrix
     *  @param from  the position to start extracting from
     */
    def col (col: Int, from: Int = 0): VectorL =
    {
        val u = new VectorL (dim1 - from)
        for (i<- from until dim1) u(i-from) = v(col)(i)
        u
    } // col

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set column 'col' of the matrix to a vector.
     *  @param col  the column to set
     *  @param u    the vector to assign to the column
     */
    def setCol (col: Int,u: VectoL) 
    { 
        v (col) = if (u.isInstanceOf [RleVectorL]) u.asInstanceOf [RleVectorL] 
                  else RleVectorL (u) 
    } // setCol

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Select columns from 'this' matrix according to the given index/basis.
     *  Ex: Can be used to divide a matrix into a basis and a non-basis.
     *  @param colIndex  the column index positions (e.g., (0, 2, 5))
     */
    def selectCols (colIndex: Array [Int]): RleMatrixL =
    {
        val c = new RleMatrixL (dim1, colIndex.length)
        for (j <- c.range2) c.v(j) = v(colIndex (j))
        c
    } // selectCols

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Transpose 'this' matrix (columns => rows).
     */
    def t: RleMatrixL = RleMatrixL (for (i <- range1) yield this(i))

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (row) vector 'u' and 'this' matrix, i.e., prepend 'u' to 'this'.
     *  @param u  the vector to be prepended as the new first row in new matrix
     */
    def +: (u: VectoL): RleMatrixL =
    {  
        if (u.dim != dim2) flaw ("+:", "vector does not match row dimension")
        RleMatrixL (for (i <- range2) yield 
            RleVectorL (for (j <- 0 until (dim1 + 1)) yield if (j == 0) u(i) else v(i)(j-1)))
    } // +:

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (column) vector 'u' and 'this' matrix, i.e., prepend 'u' to 'this'.
     *  @param u  the vector to be prepended as the new first column in new matrix
     */
    def +^: (u: VectoL): RleMatrixL = 
    {
        val m = u match {
            case _: RleVectorL => u.asInstanceOf [RleVectorL] +: v
            case _             => RleVectorL (for (i <- u.range) yield u(i)) +: v
        } // match     
        new RleMatrixL (dim1, dim2 + 1, m) 
    } // +^:
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate 'this' matrix and (row) vector 'u', i.e., append 'u' to 'this'.
     *  @param u  the vector to be appended as the new last row in new matrix
     */
    def :+ (u: VectoL): RleMatrixL =
    {  
        if (u.dim != dim2) flaw (":+", "vector does not match row dimension")
        RleMatrixL (for (i <- range2) yield 
            RleVectorL (for (j <- 0 until (dim1 + 1)) yield if (j < dim1) v(i)(j) else u(i)))
    } // :+
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate 'this' matrix and (column) vector 'u', i.e., append 'u' to 'this'.
     *  @param u  the vector to be appended as the new last column in new matrix
     */
    def :^+ (u: VectoL): RleMatrixL =
    {
        val m = u match {
            case _: RleVectorL => v :+ u.asInstanceOf [RleVectorL]
            case _             => v :+ RleVectorL (for (i <- u.range) yield u(i)) 
        } // match     
        new RleMatrixL (dim1, dim2 + 1, m) 
    } // :^+
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (row-wise) 'this' matrix and matrix 'b'. FIX
     *  @param b  the matrix to be concatenated as the new last rows in new matrix
     */
    def ++ (b: MatriL): RleMatrixL =
    {
        if (b.dim2 != dim2) flaw ("++", "matrix b does not match row dimension")
        val c = new MatrixL (dim1 + b.dim1, dim2)
        for (i <- c.range1) c(i) = if (i < dim1) this(i) else b(i - dim1)
        RleMatrixL (c)
    } // ++
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Concatenate (column-wise) 'this' matrix and matrix 'b'.
     *  @param b  the matrix to be concatenated as the new last columns in new matrix
     */
    def ++^ (b: MatriL): RleMatrixL =
    {
        if (b.dim1 != dim1) flaw ("++^", "matrix b does not match column dimension")       
        val m = b match {
        case _: RleMatrixL => v ++ b.asInstanceOf [RleMatrixL]()
        case _             => v ++ RleMatrixL (b.asInstanceOf [MatrixL])()
        } // match
        new RleMatrixL (dim1, dim2 + dim2, m)
    } // ++^
   
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and matrix 'b'.
     *  @param b  the matrix to add (requires leDimensions)
     */
    def + (b: MatriL): RleMatrixL = 
    {
         val c = new RleMatrixL (dim1, dim2) 
         b match {
         case _: RleMatrixL => for (i <- range2) c.setCol (i, col(i) + b.col(i))                                                             
         case _             => for (i <- range1; j <- range2) c(i, j) = this(i, j) + b(i, j)                                                              
         } // match
         c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and scalar 'x'.
     *  @param x  the scalar to add
     */
    def + (x: Long): RleMatrixL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = col(i) + x 
        c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add 'this' matrix and vector 'u'.
     *  @param u  the matrix to add (requires leDimensions)
     */
    def + (u: VectoL): MatriL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) + u(i)
        c
    } // +

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and scalar 'x'.
     *  @param x  the scalar to add
     */
    def += (x: Long): MatriL = 
    {
        for (i <- range2) v(i) += x
        this
    } // +=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and vector 'u'.
     *  @param u  the vector to add
     */
    def += (u: VectoL): MatriL = 
    {
        for (i <- range2) v(i) += u(i)
        this
    } // +=
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Add in-place 'this' matrix and matrix 'b'.
     *  @param b  the matrix to add (requires 'leDimensions')
     */
    def += (b: MatriL): MatriL = 
    {
        b match {
        case _: RleMatrixL => for (i <- range2) col(i) += b.col(i)
        case _             => for (i <- range1; j <- range2) v(i)(j) += b(i, j) 
        } // match     
        this
    } // +=
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get size of each column of 'this' RleMatrix
     */
    def csize: VectorI = VectorI (for (j <- range2) yield v(j).csize) 

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract scalar 'x'.
     *  @param x the scalar to subtract
     */
    def - (x: Long): MatriL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) - x
        c
    } // -

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract vector 'u'.
     *  @param u the vector to subtract
     */
    def - (u: VectoL): RleMatrixL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) - u(i)
        c
    } // -
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract matrix 'b'.
     *  @param b the matrix to subtract
     */
    def - (b: MatriL): MatriL = 
    {
         val c = new RleMatrixL (dim1, dim2) 
         b match {
         case _: RleMatrixL => for (i <- range2) c.setCol (i, col(i) - b.col(i))                                                             
         case _             => for (i <- range1; j <- range2) c(i, j) = this(i, j) - b(i, j)                                                              
         } // match
         c
    } // -
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place scalar 'x'.
     *  @param x  the scalar to subtract
     */
    def -= (x: Long): MatriL = 
    {
        for (i <- range2) v(i) -= x
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place vector 'u'.
     *  @param u the vector to subtract
     */
    def -= (u: VectoL): MatriL = 
    {
        for (i <- range2) v(i) -= u(i)
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** From 'this' matrix subtract in-place matrix 'b'.
     *  @param b  the matrix to subtract (requires 'leDimensions')
     */
    def -= (b: MatriL): MatriL = 
    {
        b match {
        case _: RleMatrixL => for (i <- range2) col(i) -= b.col(i)
        case _             => for (i <- range1; j <- range2) v(i)(j) -= b(i, j) 
        } // match     
        this
    } // -=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Raise 'this' matrix to the 'p'th power (for some integer 'p' >= 2).
     *  FIX - make compatible with imple in BldMatrix
     *  @param p  the power to raise 'this' matrix to
     */  
    def ~^ (p: Int): RleMatrixL = 
    {
        if (p < 2)        flaw ("~^", "p must be an integer >= 2")
        if (dim1 != dim2) flaw ("~^", "Only defined on square matrices")

        var t    = p
        var res  = new RleMatrixL (dim1, dim2)
        var temp = new RleMatrixL (dim1, dim2)
        for (i <- range1; j <- range2) {
            temp(i, j) = this(i, j)
            res(i, j)  = if (i == j) 1l else 0l
        } // for
        while (t > 0) {
            if (t % 2 == 1) res *= temp
            temp *= temp
            t = t / 2
        } // while
        res
    } // ~^

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by (column) vector 'u'
     *  @param u  the vector to multiply by
     */
    def mul2 (u: RleVectorL): RleVectorL =
    {      
        val c = new RleVectorL (dim1)
        for (i <- range1) {
            var sum = 0l
            for (k <- range2) sum += this(i, k) * u(k)
            c(i) = sum
        } // for
        c
    } // mul2
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by (column) vector 'u'
     *  @param u  the vector to multiply by
     */
    def * (u: VectoL): VectoL = 
    {
        if (dim2 > u.dim) flaw ("*", "matrix * vector - vector dimension too small")

        u match {
        case _: RleVectorL => mul2 (u.asInstanceOf [RleVectorL])
        case _             => mul (this, u)
        } // match
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by scalar 'x'.
     *  @param x  the scalar to multiply by
     */
    def * (x: Long): RleMatrixL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) * x
        c
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by matrix 'x'
     *  @param x  the matrix to multiply by 
     */
    def *= (x: Long): RleMatrixL = 
    {
        for (i <- range2) v(i) *= x
        this
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by matrix 'b'.
     *  @param b  the matrix to multiply by (requires 'sameCrossDimensions')
     */
    def * (b: MatriL): MatriL =
    {
        b match {
        case _: RleMatrixL => t mdot b.asInstanceOf [RleMatrixL]
        case _             => toDense * b
        } // match
    } // *

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by matrix 'b'
     *  @param b  the matrix to multiply by (requires square and 'sameCrossDimensions')
     */
    def *= (b: MatriL): MatriL = 
    {
        if (! b.isSquare)   flaw ("*=", "matrix 'b' must be square")
        if (dim2 != b.dim1) flaw ("*=", "matrix *= matrix - incompatible cross dimensions")
        
        val temp = new RleMatrixL (b.dim1, b.dim2)
        for (i <- 0 until b.dim1; j <- 0 until b.dim2) temp(i, j) = b(i, j)
        for (i <- range1) {
            val row_i = new VectorL (dim2)
            for (j <- range2) row_i(j) = this(i, j)
            for (j <- 0 until b.dim2) {
                var sum = 0l
                for (k <- range2) sum += (row_i(k) * temp(k, j))
                this(i, j) = sum
            } // for
        } // for
        this
    } // *=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply 'this' matrix by vector 'u' to produce another matrix 'a_ij * u_j'.
     *  E.g., multiply a matrix by a diagonal matrix represented as a vector.
     *  @param u  the vector to multiply by
     */
    def ** (u: VectoL): MatriL = 
    {
        val dm = math.min (dim2, u.dim)
        var c  = new RleMatrixL (dim1, dm)
        for (i <- range1; j <- c.range2) c(i, j) = this(i, j) * u(j)
        c
    } // **

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply in-place 'this' matrix by vector 'u' to produce another matrix 'a_ij * u_j'.
     *  @param u  the vector to multiply by
     */
    def **= (u: VectoL): MatriL = 
    {
        if (dim2 > u.dim) flaw ("**=", "vector u not large enough")
        for (i <- range1; j <- range2) this(i, j) = this(i, j) * u(j)
        this
    } // **=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Multiply vector 'u' by 'this' matrix to produce another matrix 'u_i * a_ij'.
     *  E.g., multiply a diagonal matrix represented as a vector by a matrix.
     *  This operator is right associative.
     *  @param u  the vector to multiply by
     */
    def **: (u: VectoL): MatriL =
    {
        val dm = math.min (dim2, u.dim)
        val c  = new RleMatrixL (dim1, dm)
        for (i <- range1; j <- c.range2) c(i, j) = u(i) * this(i, j)
        c
    } // **:

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide 'this' matrix by scalar 'x'.
     *  @param x the scalar to divide by
     */
    def / (x: Long): MatriL = 
    {
        val c = new RleMatrixL (dim1, dim2)
        for (i <- range2) c.v(i) = v(i) / x
        c
    } // /

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Divide in-place 'this' matrix by scalar 'x'.
     *  @param x  the scalar to divide by
     */
    def /= (x: Long): MatriL = 
    {
        for (i <- range2) v(i) /= x
        this
    } // /=

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Clean values in 'this' matrix at or below the threshold 'thres' by setting
     *  them to zero.  Iterative algorithms give approximate values and if very close
     *  to zero, may throw off other calculations, e.g., in computing eigenvectors.
     *  @param thres     the cutoff threshold (a small value)
     *  @param relative  whether to use relative or absolute cutoff
     */
    def clean (thres: Double = TOL, relative: Boolean = true): MatriL = 
    {
        val s = if (relative) mag else 1l              // use matrix magnitude or 1
        for (i <- range1; j <- range2) if (ABS (this(i, j)) <= thres * s) this(i, j) = 0l
        this
    } // clean

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the determinant of 'this' matrix.  The value of the determinant
     *  indicates, among other things, whether there is a unique solution to a
     *  system of linear equations (a nonzero determinant).
     */ 
    def det: Long = 
    {
        if (dim1 != dim2) flaw ("det", "determinant only works on square matrices")
        
        var sum = 0l
        var b: MatrixL = null
        for (j <- range2) {
            b = (this.toDense).sliceExclude (0, j)   // the submatrix that excludes row 0 and column j
            sum += (if (j % 2 == 0) this(0, j) * (if (b.dim1 == 1) b(0, 0) else b.det)
                    else           -this(0, j) * (if (b.dim1 == 1) b(0, 0) else b.det))
        } // for 
        sum
    } // det

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Form a matrix '[Ip, this, Iq]' where Ir is a 'r-by-r' identity matrix, by
     *  positioning the three matrices 'Ip', 'this' and 'Iq' along the diagonal.
     *  Fill the rest of matrix with zeros.
     *  @param p  the size of identity matrix Ip
     *  @param q  the size of identity matrix Iq
     */
    def diag (p: Int,q: Int = 0): MatriL = 
    {
        if (! isSquare) flaw ("diag", "'this' matrix must be square")
        var c = new RleMatrixL (p + dim1 + q, p + dim2 + q)
        var Ip = eye (p)
        var Iq = eye (q)
        c = Ip diag (this) diag (Iq)
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Combine 'this' matrix with matrix 'b', placing them along the diagonal and
     *  filling in the bottom left and top right regions with zeros; '[this, b]'.
     *  @param b  the matrix to combine with 'this' matrix
     */
    def diag (b: MatriL): RleMatrixL = 
    {
        var c = new RleMatrixL (dim1 + b.dim1, dim2 + b.dim2)
        var x1 = new RleVectorL (b.dim2)
        for (i <- range1) c(i) = this(i) ++ x1
        val x2 = new RleVectorL (dim2)
        for (i <- 0 until b.dim1) c(i + dim1) = x2 ++ b(i)
        c
    } // diag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and vector 'b', by first transposing
     *  'this' matrix and then multiplying by 'b' (i.e., 'a dot u = a.t * b').
     *  @param b  the vector to multiply by (requires same first dimensions)
     */
    def dot (b: VectoL): VectoL = 
    {
        if (dim1 != b.dim) flaw ("dot", "matrix dot vector - incompatible first dimensions")
        val c = new VectorL (dim2)
        val at = this.t
        for (i <- range2) {
            var sum = 0l
            for (j <- range1) sum += at(i, j) * b(j)
            c(i) = sum
        } // for
        c
    } // dot

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and matrix 'b'. Results in a Vector.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def dot (b: MatriL): RleVectorL = 
    {
        if (dim1 != b.dim1) flaw ("dot", "incompatible")        
        RleVectorL (for (j <- range2) yield  v(j) dot b.col(j))
    } // dot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the dot product of 'this' matrix and matrix 'b'. Results in a Vector.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def dot (b: RleMatrixL): RleVectorL = 
    {
        if (dim1 != b.dim1) flaw ("dot", "incompatible")        
        RleVectorL (for (j <- range2) yield  v(j) dot b.v(j))
    } // dot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the matrix dot product of 'this' matrix and matrix 'b'.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def mdot (b: MatriL): RleMatrixL = 
    {   
        if (dim1 != b.dim1) flaw ("mdot", "incompatible first dimensions")
        val vv = Array.ofDim [RleVectorL] (d2)   
        for (j <- b.range2) vv(j) = RleVectorL (for (i <- range2) yield  v(i) dot b.col(j))
        new RleMatrixL (dim2, b.dim2, vv)
    } // mdot
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the matrix dot product of 'this' matrix and matrix 'b'.
     *  @param b  the matrix to multiply by (requires same first dimensions)
     */
    def mdot (b: RleMatrixL): RleMatrixL = 
    {   
        if (dim1 != b.dim1) flaw ("mdot", "incompatible first dimensions")
        val vv = Array.ofDim [RleVectorL] (d2)   
        for (j <- b.range2) vv(j) = RleVectorL (for (i <- range2) yield  v(i) dot b.v(j))
        new RleMatrixL (dim2, b.dim2, vv)
    } // mdot

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Get the 'k'th diagonal of 'this' matrix.
     *  @param k  how far above the main diagonal, e.g., (-1, 0, 1) for (sub, main, super)
     */
    def getDiag (k: Int = 0): RleVectorL = 
    {
        var x = new RleVectorL (1)
        var i, j = 0
        if (k >= 0) { i = 0;      j = k }
        else        { i = -1 * k; j = 0 }
        x(0) = this(i, j)
        i += 1; j += 1    
        while (i < dim1 && j < dim2) {
            x = x ++ this(i, j)
            i += 1; j += 1       
        } // while
        x
    } // getDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert 'this' matrix (requires a square matrix) and use partial pivoting.
     */
    def inverse: MatriL = 
    {
        var b = new RleMatrixL (dim1, dim2)
        var u = eye (dim1)
        for (i <- range1; j <- range2) b(i, j) = this(i, j)
        b = b ++^ u
        b.reduce_ip
        b.sliceCol (dim2, 2 * dim2)
    } // inverse

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Invert in-place 'this' matrix (requires a square matrix) and uses partial pivoting.
     *  Note: this method turns the original matrix into the identity matrix.
     *  The inverse is returned and is captured by assignment.
     */
    def inverse_ip: MatriL = 
    {
        var b = this
        var u = eye (dim1)
        b = b ++^ u
        b.reduce_ip
        b.sliceCol (dim2, 2 * dim2)
    } // inverse_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Check whether 'this' matrix is rectangular (all rows have the same number
     *  of columns).
     */
    def isRectangular: Boolean = 
    {
        for (j <- 0 until dim2) if (v(j).size != dim1) return false
        true
    } // isRectangular

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the lower triangular of 'this' matrix (rest are zero).
     */ 
    def lowerT: RleMatrixL = 
    {
        var x = new RleMatrixL (dim1, dim2)
        for (i <- range1; j <- 0 to i) x(i, j) = this(i, j)
        x
    } // lowerT
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Return the upper triangular of 'this' matrix (rest are zero).
     */ 
    def upperT: RleMatrixL = 
    {
        var x = RleMatrixL (new MatrixL (dim1, dim2))
        for (i <- range1; j <- i until dim2) x(i, j) = this(i, j)
        x
    } // upperT

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use partial pivoting to find a maximal non-zero pivot and return its row
     *  index, i.e., find the maximum element '(k, i)' below the pivot '(i, i)'.
     *  @param a  the matrix to perform partial pivoting on
     *  @param i  the row and column index for the current pivot
     */
    private def partialPivoting (a: RleMatrixL, i: Int): Int =
    {
        var max  = a.v(i)(i)         // initially set to the pivot
        var kMax = i                 // initially the pivot row

        for (k <- i + 1 until a.dim1 if ABS (a.v(k)(i)) > max) {
            max  = ABS (a.v(k)(i))
            kMax = k
        } // for

        if (kMax == i) {
            flaw ("partialPivoting", "unable to find a non-zero pivot for row " + i)
        } // if
        kMax
    } // partialPivoting

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' using back substitution in the equation 'u*x = y' where
     *  'this' matrix ('u') is upper triangular (see 'lud_npp' above).
     *  @param y  the constant vector
     */
    def bsolve (y: VectoL): RleVectorL = 
    {
        var x = new RleVectorL (dim2)
        for (i <- dim1 - 1 to 0 by -1) {
            var sum = 0l
            for (j <- i + 1 until dim2) sum += this(i, j) * x(j)
            x(i) = (y(i) - sum) / this(i, i)
        } // for
        x
    } // bsolve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Factor 'this' matrix into the product of lower and upper triangular
     *  matrices '(l, u)' using an 'LU' Factorization algorithm.
     *  FIX - check for 0 pivots (divide by zero).
     */
    def lud_npp: (RleMatrixL, RleMatrixL) = 
    {
        if (! isSquare) throw new IllegalArgumentException ("lud_npp: requires a square matrix")

        val l = new RleMatrixL (dim1)
        val u = new RleMatrixL (dim1)

        for (i <- range1) l(i,i) = 1
        for (j <- range2) u(0, j) = this(0, j)
        for (i <- 1 until dim1) l(i, 0) = this(i, 0) / u(0, 0)
        for (i <- 1 until dim1; j <- 1 until dim2) {
            var sum = 0l
            if (i > j) {                                   // find l(i, j)
                for (k <- 0 until j) sum += l(i, k) * u(k, j)
                l(i, j) = (this(i, j) - sum) / u(j, j)
            } // if
            if (i <= j) {                                  // find u(i, j)
                for (k <- 0 until j) sum += l(i, k) * u(k, j)
                u(i, j) = this(i, j) - sum
            } // if
        } // for
        (l, u)
    } // lud_npp

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Factor in-place 'this' matrix into the product of lower and upper triangular
     *  matrices '(l, u)' using an 'LU' Factorization algorithm.
     *  FIX - check for 0 pivots (divide by zero).
     */
    def lud_ip: (RleMatrixL, RleMatrixL) = 
    {
        if (! isSquare) throw new IllegalArgumentException ("lud_ip: requires a square matrix")

        var w = new VectorL (dim1)
        for (k <- range1) {
            for (j <- k+1 until dim2) w(j) = this(k, j)
            for (i <- k+1 until dim1) {
                var a = this(i, k) / this(k, k)
                this(i,k) = a
                for (j <- k+1 until dim2) this(i, j) = this(i, j) - (a * w(j))
            } // for
        } // for
        var l = new RleMatrixL (dim1)
        for (i <- range1; j <- 0 to i) {         
            if (i == j) l(i, j) = 1
            else if (i > j) {
                l(i, j) = this(i, j)
                this(i, j) = 0l
            } // if
        } // for
        (l, this)
    } // lud_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the maximum element in 'this' matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def max (e: Int = dim1): Long = 
    {
        if (e <= 0) flaw ("max", "the ending index e can't be negative")
        var x = v(0)(0)
        for (j <- range2) x = MAX (x, v(j) max (e))
        x
    } // max

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Find the minimum element in 'this' matrix.
     *  @param e  the ending row index (exclusive) for the search
     */
    def min (e: Int = dim1): Long = 
    {
        if (e <= 0) flaw ("min", "the ending index e can't be negative")
        var x = v(0)(0)
        for (j <- range2) x = MIN (x, v(j) min (e))
        x
    } // min

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the (right) nullspace of 'this' 'm-by-n' matrix (requires 'n = m+1')
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.
     *  <p>
     *      nullspace (a) = set of orthogonal vectors v s.t. a * v = 0
     *  <p>
     *  The left nullspace of matrix 'a' is the same as the right nullspace of 'a.t'.
     *  FIX: need a more robust algorithm for computing nullspace (@see Fac_QR.scala).
     *  FIX: remove the 'n = m+1' restriction.
     *  @see http://ocw.mit.edu/courses/mathematics/18-06sc-linear-algebra-fall-2011/ax-b-and-the-four-subspaces
     *  @see /solving-ax-0-pivot-variables-special-solutions/MIT18_06SCF11_Ses1.7sum.pdf
     */ 
    def nullspace: VectoL = 
    {
        if (dim2 != dim1 + 1) flaw ("nullspace", "requires n(columns) = n(rows) + 1")
        reduce.col (dim2 - 1) * -1l ++ 1l
    } // nullspace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute in-place the (right) nullspace of 'this' 'm-by-n' matrix (requires 'n = m+1')
     *  by performing Gauss-Jordan reduction and extracting the negation of the
     *  last column augmented by 1.
     *  <p>
     *      nullspace (a) = set of orthogonal vectors v s.t. a * v = 0
     *  <p>
     *  The left nullspace of matrix 'a' is the same as the right nullspace of 'a.t'.
     *  FIX: need a more robust algorithm for computing nullspace (@see Fac_QR.scala).
     *  FIX: remove the 'n = m+1' restriction.
     *  @see http://ocw.mit.edu/courses/mathematics/18-06sc-linear-algebra-fall-2011/ax-b-and-the-four-subspaces
     *  @see /solving-ax-0-pivot-variables-special-solutions/MIT18_06SCF11_Ses1.7sum.pdf
     */
    def nullspace_ip: VectoL = 
    {
        if (dim2 != dim1 + 1) flaw ("nullspace_ip", "requires n(columns) = n(rows) + 1")
        reduce_ip ()
        col (dim2 - 1) * -1l ++ 1l
    } // nullspace_ip
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction on 'this' matrix to make the left part embed an
     *  identity matrix.  A constraint on this 'm-by-n' matrix is that 'n >= m'.
     *  It can be used to solve 'a * x = b': augment 'a' with 'b' and call reduce.
     *  Takes '[a | b]' to '[I | x]'.
     */
    def reduce: RleMatrixL = 
    {
        if (dim2 < dim1) flaw ("reduce", "requires n(columns) >= n(rows)")
            
        val b = new RleMatrixL (dim1, dim2)
        for (i <- range1; j <- range2) b(i, j) = this(i, j)
        for (i <- range1) {
            var x = b(i, i)
            if (x =~ 0l) flaw ("reduce", "unable to find a non-zero pivot in row " + i)
            for (j <- range2) b(i, j) = b(i, j) / x
            for (k <- range1) {
                if (k != i && b(k, i) != 0l) {
                    var mul = b(k, i)
                    for (l <- range2) b(k, l) -= mul * b(i, l)
                } // if
            } // for
        } // for
        b
    } // reduce

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Use Gauss-Jordan reduction in-place on 'this' matrix to make the left part
     *  embed an identity matrix.  A constraint on this 'm-by-n' matrix is that 'n >= m'.
     *  It can be used to solve 'a * x = b': augment 'a' with 'b' and call reduce.
     *  Takes '[a | b]' to '[I | x]'.
     */
    def reduce_ip ()
    {
        if (dim2 < dim1) flaw ("reduce", "requires n (columns) >= m (rows)")
        
        val b = this
        for (i <- range1) {
            var pivot = b(i, i)
            if (pivot =~ 0l) {
                val k = partialPivoting (b, i)  // find the maxiumum element below pivot
                b.swap (i, k, i)                // in b, swap rows i and k from column i
                pivot = b.v(i)(i)               // reset the pivot
            } // if
            for (j <- range2) b(i, j) = b(i, j) / pivot
            for (k <- range1) {
                if (k != i && b(k, i) != 0l) {
                    var mul = b(k, i)
                    for (l <- range2) b(k, l) -= mul * b(i, l)
                } // if
            } // for
        } // for
    } // reduce_ip

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set the main diagonal of 'this' matrix to the scalar 'x'.
     *  @param x  the scalar to set the diagonal to
     */
    def setDiag (x: Long)
    {
        val diagRange = if (dim1 < dim2) 0 until dim1 else 0 until d2
        for (i <- diagRange) v(i)(i) = x
    } // setDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Set the 'k'th diagonal of 'this' matrix to the vector 'u'.  Assumes 'dim2 >= dim1'.
     *  @param u  the vector to set the diagonal to
     *  @param k  how far above the main diagonal, e.g., (-1, 0, 1) for (sub, main, super)
     */
    def setDiag (u: VectoL, k: Int = 0)
    {
        var (i, j, z) = (0, 0, 0)
        var dsize = MIN (dim1, dim2)
        if (k > 0) {
            i = 0; j = k
            dsize = MIN (dim1, dim2 - j)
        } // if
        if (k < 0) {
            i = -k; j = 0           
            dsize = MIN (dim1 - i, dim2)
        } // if
        
        if (dsize != u.size) flaw ("setDiag","Vector 'u' must contain " + dsize + " element(s)")
        
        while (i < dim1 && j < dim2) {
            this(i, j) = u(z)
            z += 1; i += 1; j += 1;
        } // while
    } // setDiag

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'l*u*x = b'
     *  @param l  the lower triangular matrix
     *  @param u  the upper triangular matrix
     *  @param b  the constant vector
     */
    def solve (l: MatriL, u: MatriL, b: VectoL): VectoL = 
    {
        var y = new RleVectorL (l.dim2)
        for (i <- 0 until y.dim) {
            var sum = 0l
            for (j <- 0 until i) sum += l(i, j) * y(j)
            y(i) = b(i) - sum
        } // for
        u.bsolve (y)
    } // solve

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Solve for 'x' in the equation 'a*x = b' where 'a' is 'this' matrix.
     *  @param b  the constant vector.
     */ 
    def solve (b: VectoL): VectoL = solve (lud_npp, b)
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of 'this' matrix, i.e., the sum of its elements.
     */
    def sum: Long = 
    {
        var sum = 0l
        for (i <- range2) sum += v(i).sum
        sum
    } // sum

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the 'abs' sum of 'this' matrix, i.e., the sum of the absolute value
     *  of its elements.  This is useful for comparing matrices '(a - b).sumAbs'.
     */
    def sumAbs: Long = 
    {
        var sum = 0l
        for (i <- range2) sum += v(i).norm1
        sum
    } // sumAbs

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the sum of the lower triangular region of 'this' matrix.
     */
    def sumLower: Long = 
    {
        var sum = 0l
        for (i <- range2; j <- i + 1 until dim1) sum += v(i)(j)
        sum
    } // sumLower

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Compute the trace of 'this' matrix, i.e., the sum of the elements on the
     *  main diagonal.  Should also equal the sum of the eigenvalues.
     *  @see Eigen.scala
     */
    def trace: Long = 
    {
        if ( ! isSquare) flaw ("trace", "trace only works on square matrices")
        var sum = 0l
        for (i <- range1) sum += v(i)(i)
        sum
    } // trace

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Write 'this' matrix to a CSV-formatted text file with name 'fileName'.
     *  @param fileName  the name of file to hold the data
     */
    def write (fileName: String)
    {
        val out = new PrintWriter (fileName)
        for (i <- range1) {
            for (j <- range2) {
                out.print (this(i, j))
                if (j < dim2 - 1) out.print (",")
            } // for
            out.println ()
        } // for
        out.close ()
    } // write

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Convert 'this' real (Long precision) matrix to a string.
     */
    override def toString: String = 
    {
        val sb = new StringBuilder ("\nRleMatrixL (")
        if (dim1 == 0) return sb.append (")").mkString
        for (j <- range2) {
            if (j == 0) sb.append ("\t"+v(j) + "\n")
            else        sb.append ("\t\t"+v(j) + "\n")
        } // for
        sb.replace (sb.length-3, sb.length, ")").mkString
    } // toString
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Override equals to determine whether 'this' vector equals vector 'b'.
     *  @param b  the vector to compare with this
     */
    override def equals (b: Any): Boolean =
    {
        b.isInstanceOf [RleMatrixL] && (v.deep equals b.asInstanceOf [RleMatrixL].v.deep)
    } // equals

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Must also override hashCode for 'this' vector to be compatible with equals.
     */
    override def hashCode: Int = v.deep.hashCode

} // RleMatrixL class


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `RleMatrixL` companion object provides operations for `RleMatrixL` that don't require
 *  'this' (like static methods in Java).  It provides factory methods for building
 *  matrices from sequences or vectors.
 */
object RleMatrixL 
{
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: RleVectorL, xs: RleVectorL*): RleMatrixL = 
    {     
        new RleMatrixL (xs.length + 1, x.dim, (x +: xs).toArray)    
    } // apply
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: Seq [RleVectorL]): RleMatrixL = 
    {
        new RleMatrixL (x(0).dim, x.length, x.toArray)
    } // apply
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (u: Vector [VectoL]): RleMatrixL = 
    {
        val u_dim = u(0).dim
        val x = new RleMatrixL (u_dim, u.length)
        for (j <- 0 until u.length) x.setCol (j, u(j)) 
        x
    } // apply

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def apply (x: MatrixL): RleMatrixL = 
    {
        RleMatrixL (for (j <- 0 until x.dim2) yield RleVectorL (x.col(j)))
    } // apply

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /** Create an 'm-by-n' identity matrix I (ones on main diagonal, zeros elsewhere).
     *  If 'n' is <= 0, set it to 'm' for a square identity matrix.
     *  FIX: store as a diagonal matrix.
     *  @param m  the row dimension of the matrix
     *  @param n  the column dimension of the matrix (defaults to 0 => square matrix)
     */
    def eye (m: Int, n: Int = 0): RleMatrixL =
    {
        val nn = if (n <= 0) m else n             // square matrix, if n <= 0
        val mn = if (m <= nn) m else nn           // length of main diagonal
        val c = new RleMatrixL (m, nn)
        for (i <- 0 until mn) c(i, i) = 1l
        c
    } // eye
  
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def mul (at: RleMatrixL, u: VectoL): RleVectorL = 
    {
        RleVectorL (for (i <- 0 until at.dim1) yield at(i) dot u) 
    } // mul

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     */
    def mul (a: RleMatrixL, u: RleVectorL): RleVectorL = 
    {
        RleVectorL (for (i <- 0 until a.dim1) yield a(i) dot u)    
    } // mul
       
} // RleMatrixL object


//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
/** The `RleMatrixLTest` object tests the operations provided by `MatrixL` class.
 *  > runMain scalation.linalgebra.RleMatrixLTest
 */
object RleMatrixLTest extends App
{
    for (l <- 1 to 4) {
        println ("\n\tTest RleMatrixL on real matrices of dim " + l)
        val x = new RleMatrixL (l, l)
        val y = new RleMatrixL (l, l)
        x.set (2)
        y.set (3)
        println ("x + y = " + (x + y))
        println ("x - y = " + (x - y))
        println ("x * y = " + (x * y))
        println ("x * 4 = " + (x * 4))
    } // for

    println ("\n\tTest RleMatrixL on additional operations")

    val z   = RleMatrixL (new MatrixL ((2, 2), 1, 2,
                                               3, 2))
    val t   = RleMatrixL (new MatrixL ((3, 3), 1, 2, 3,
                                               4, 3, 2,
                                               1, 3, 1))
    val zz  = RleMatrixL (new MatrixL ((3, 3), 3, 1, 0,
                                               1, 4, 2,
                                               0, 2, 5))
    val bz  = VectorL (5, 3, 6)
    val b   = VectorL (8, 7)
    val lu  = z.lud_npp

    println ("z            = " + z)
    println ("z.t          = " + z.t)
    println ("z.lud_npp    = " + lu)
    println ("z.solve      = " + z.solve (lu._1, lu._2, b))
    println ("zz.solve     = " + zz.solve (zz.lud_npp, bz))
    println ("z.inverse    = " + z.inverse)
    println ("z.inverse_ip = " + z.inverse_ip ())
    println ("t.inverse    = " + t.inverse)
    println ("t.inverse_ip = " + t.inverse_ip ())
    println ("z.inv * b    = " + z.inverse * b)
    println ("z.det        = " + z.det)
    println ("z            = " + z)
    z *= z                             // in-place matrix multiplication
    println ("z squared = " + z)

    val w = RleMatrixL (new MatrixL ((2, 3), 2,  3, 5, 
                                            -4,  2, 3))
    val v = RleMatrixL (new MatrixL ((3, 2), 2, -4, 
                                             3,  2,
                                             5,  3))
    println ("w         = " + w)
    println ("v         = " + v)
    println ("w.reduce  = " + w.reduce)

    println ("right:    w.nullspace = " + w.nullspace)

    println ("check right nullspace = " + w * w.nullspace)
    println ("left:   v.t.nullspace = " + v.t.nullspace)

    for (row <- z) println ("row = " + row.deep)

    val aa = RleMatrixL (new MatrixL ((3, 2), 1, 2,
                                              3, 4,
                                              5, 6))
    val bb = RleMatrixL (new MatrixL ((2, 2), 1, 2,
                                              3, 4))
    val cc = RleMatrixL (new MatrixL ((3, 2), 3, 4,
                                              5, 6,
                                              7, 8))

    println ("aa        = " + aa)
    println ("bb        = " + bb)
    println ("aa * bb   = " + aa * bb)
    println ("aa dot bz = " + (aa dot bz))
    println ("aa.t * bz = " + aa.t * bz)

    println ("aa dot cc   = " + (aa dot cc))
    println ("aa.t * cc   = " + aa.t * cc)

    println ("Dense aa" + aa.toDense)
    println ("Dense bb" + bb.toDense)
    aa *= bb
    println ("aa *= bb  = " + aa)

    val filename = scalation.DATA_DIR + "bb_matrix.csv"
    bb.write (filename)
    println ("bb_csv = " + MatrixL (filename))

} // RleMatrixLTest object

