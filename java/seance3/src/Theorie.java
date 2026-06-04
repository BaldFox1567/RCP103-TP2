/**
 * Theorie.java — Formules analytiques des files d'attente (pour comparaison).
 *
 *   - M/M/1 (file infinie) : formules fermées classiques.
 *   - M/M/c/K (c serveurs, capacité K) : modèle de naissance-mort fini, qui
 *     couvre aussi M/M/1/K (c=1).
 */
public class Theorie {

    /** Résultat théorique. L = -1 et W = -1 signalent un système instable. */
    public static class Resultat {
        public double rho, L, Lq, W, Wq, pRejet;
        public boolean stable = true;
    }

    /** M/M/1 file infinie. stable=false si rho >= 1. */
    public static Resultat mm1(double lam, double mu) {
        Resultat r = new Resultat();
        r.rho = lam / mu;
        if (r.rho >= 1.0) {           // instable : pas de régime stationnaire
            r.stable = false;
            r.L = r.Lq = r.W = r.Wq = Double.POSITIVE_INFINITY;
            return r;
        }
        r.L  = r.rho / (1 - r.rho);
        r.Lq = r.rho * r.rho / (1 - r.rho);
        r.W  = 1.0 / (mu - lam);
        r.Wq = r.rho / (mu - lam);
        r.pRejet = 0.0;
        return r;
    }

    /** M/M/c/K : c serveurs, capacité totale K (file + service). Toujours stable. */
    public static Resultat mmck(double lam, double mu, int c, int K) {
        double a = lam / mu;          // charge offerte

        // Probabilités non normalisées des états n = 0..K
        double[] termes = new double[K + 1];
        for (int n = 0; n <= K; n++) {
            if (n < c) termes[n] = Math.pow(a, n) / factorielle(n);
            else       termes[n] = Math.pow(a, n) / (factorielle(c) * Math.pow(c, n - c));
        }
        double somme = 0.0;
        for (double t : termes) somme += t;

        double[] P = new double[K + 1];
        for (int n = 0; n <= K; n++) P[n] = termes[n] / somme;

        Resultat r = new Resultat();
        r.pRejet = P[K];                       // proba système plein
        double lamEff = lam * (1 - r.pRejet);  // taux réellement admis
        double L = 0.0;
        for (int n = 0; n <= K; n++) L += n * P[n];
        r.L  = L;
        r.Lq = L - lamEff / mu;                // serveurs occupés moyens = lamEff/mu
        r.W  = L / lamEff;
        r.Wq = r.Lq / lamEff;
        r.rho = a / c;                         // utilisation par serveur
        return r;
    }

    private static double factorielle(int n) {
        double f = 1.0;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }
}
