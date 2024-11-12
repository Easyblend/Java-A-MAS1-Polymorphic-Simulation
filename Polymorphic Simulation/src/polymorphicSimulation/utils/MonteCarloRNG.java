package polymorphicSimulation.utils;


public class MonteCarloRNG {
    private long seed;
    private final long multiplier = 1597;
    private final long increment = 51749;
    private final long modulus = 244944;
    private int currentIteration = 0;

    /**
     * Constructs a MonteCarloRNG with a specified
     * 
     * @param seed      The initial seed for the random number generator
     */
    public MonteCarloRNG(long seed) {
        this.seed = seed;
    }

    /**
     * Constructs a MonteCarloRNG with a seed based on the current system time (default constructor).
     */
    public MonteCarloRNG() {
        this(System.currentTimeMillis());
    }

    /**
     * Computes the approximation of the error function (erf) for a given value x.
     * See: https://en.wikipedia.org/wiki/Error_function
     * 
     * This approximation is accurate to about 1.2e-7 for |x| < 4.0.
     *
     * @param x The input value for which to compute the error function.
     * @return The approximate value of erf(x).
     */
    private double erf(double x) {
        // Constants for approximation
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        // Preserve the original sign of `x`
        int sign = (x > 0) ? 1 : -1;
        x = Math.abs(x);

        // Formula 7.1.26 from Abramowitz and Stegun
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 / (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }

    /**
     * Generates the next random number using the Linear Congruential Generator (LCG) algorithm.
     * 
     * @return the next random number in the sequence.
     */
     private long nextLCG() {
        seed = (multiplier * seed + increment) % modulus;
        return seed;
     }

     /**
      * Uses the Box-Muller transform to generate two independent standard normally distributed random numbers.
      *
      *@return an array containing two normally distributed random numbers
      */
      private double[] boxMuller() {
        double u1 = nextLCG() / (double) modulus;
        double u2 = nextLCG() / (double) modulus;

        double mag = Math.sqrt(-2.0 * Math.log(u1));
        double z1 = mag * Math.cos(2 * Math.PI * u2);
        double z2 = mag * Math.sin(2 * Math.PI * u2);

        return new double[]{z1, z2};
      }

      /**
       * Generates a random number following a standard normal distribution, transformed into the range [0, 1]
       * using the probability integral transform.
       * 
       * @return a random number in the range [0, 1]
       */
      public double nextRandom() {
        currentIteration++;
        double[] normalPair = boxMuller();

        // Using reject sampling
        double x = normalPair[0];

        // Map to [0, 1] using probability integral transform
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
      }

      /**
       * Generates a random integer within a specified range [min, max]
       * 
       * @param min     The minimum value of the range (inclusive)
       * @param max     The maximum value of the range (inclusive)
       * @return a random number within the specified range.
       */
      public int nextInt(int min, int max) {
        return min + (int) (nextRandom() * (max - min + 1));
      }

      /**
       * Returns the number of iterations performed by the generator.
       * 
       * @return the number of iterations performed.
       */
      public int getIterationCount() {
        return currentIteration;
      }

}