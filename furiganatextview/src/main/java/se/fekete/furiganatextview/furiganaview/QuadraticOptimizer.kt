/*
 * FuriganaView widget
 * Copyright (C) 2013 sh0 <sh0@yutani.ee>
 * Licensed under Creative Commons BY-SA 3.0
 */

package se.fekete.furiganatextview.furiganaview

// Constraint optimizer class
class QuadraticOptimizer(private var a: Array<FloatArray>, private var b: FloatArray) {

    // Calculate
    fun calculate(x: FloatArray) {
        // Check if calculation needed
        if (phi(1.0f, x) == 0.0f) {
            return
        }

        // Calculate
        var sigma = 1.0f

        for (k in 0 until penaltyRuns) {
            newtonSolve(x, sigma)
            sigma *= sigmaMul
        }
    }

    private fun newtonSolve(x: FloatArray, sigma: Float) {
        for (i in 0 until newtonRuns) {
            newtonIteration(x, sigma)
        }
    }

    private fun newtonIteration(x: FloatArray, sigma: Float) {
        // Calculate gradient
        val d = FloatArray(x.size)

        for (i in d.indices) {
            d[i] = phiD1(i, sigma, x)
        }

        // Calculate Hessian matrix (symmetric)
        val h = Array(x.size) { FloatArray(x.size) }
        for (i in h.indices) {

            for (j in i until h[0].size) {
                h[i][j] = phiD2(i, j, sigma, x)
            }
        }
        for (i in h.indices) {
            for (j in 0 until i) {
                h[i][j] = h[j][i]
            }
        }

        // Linear system solver
        val p = gsSolver(h, d)

        // Iteration
        for (i in x.indices) {
            x[i] = x[i] - wolfeGamma * p[i]
        }

    }

    // Gauss-Seidel solver
    private fun gsSolver(a: Array<FloatArray>, b: FloatArray): FloatArray {
        // Initial guess
        val p = FloatArray(b.size)

        for (i in p.indices) {
            p[i] = 1.0f
        }

        for (z in 0 until gsRuns) {

            for (i in p.indices) {
                var s = 0.0f

                for (j in p.indices) {
                    if (i != j) {
                        s += a[i][j] * p[j]
                    }
                }

                p[i] = (b[i] - s) / a[i][i]
            }
        }

        // Result
        return p
    }

    // Math
    private fun dot(a: FloatArray, b: FloatArray): Float {
        assert(a.size == b.size)

        var r = 0.0f

        for (i in a.indices) {
            r += a[i] * b[i]
        }

        return r
    }

    // Cost function f(x)
    private fun f(x: FloatArray): Float {
        return dot(x, x)
    }

    // Cost function phi(x)
    private fun phi(sigma: Float, x: FloatArray): Float {
        var r = 0.0f

        for (i in x.indices) {
            r += Math.pow(Math.min(0f, dot(a[i], x) - b[i]).toDouble(), 2.0).toFloat()
        }

        return f(x) + sigma * r
    }

    private fun phiD1(n: Int, sigma: Float, x: FloatArray): Float {
        var r = 0.0f

        for (i in a.indices) {
            val c = dot(a[i], x) - b[i]

            if (c < 0) {
                r += 2.0f * a[i][n] * c
            }
        }

        return 2.0f * x[n] + sigma * r
    }

    private fun phiD2(n: Int, m: Int, sigma: Float, x: FloatArray): Float {
        var r = 0.0f

        for (i in a.indices) {
            val c = dot(a[i], x) - b[i]
            if (c < 0) {
                r += 2.0f * a[i][n] * a[i][m]
            }
        }

        return (if (n == m) 2.0f else 0.0f) + sigma * r
    }

    companion object {
        // Constants
        internal const val wolfeGamma = 0.1f
        internal const val sigmaMul = 10.0f
        internal const val penaltyRuns = 5
        internal const val newtonRuns = 20
        internal const val gsRuns = 20
    }
}
