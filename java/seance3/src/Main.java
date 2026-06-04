import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main.java — Point d'entrée de la Séance 3.
 *
 * 1) testGateway() : vérifie la logique de la passerelle (service / file / rejet
 *    / service du suivant) en injectant des messages à la main.
 * 2) testEngine()  : petite simulation courte (boucle + trace + métriques).
 * 3) Campagne de simulations : M/M/1, M/M/1/4, M/M/1/8, M/M/3/8 pour
 *    lambda in {4,6,8,12}, mu=8, comparées à la théorie.
 *
 * Lancer depuis le dossier java/seance3/ (les résultats vont dans results/).
 */
public class Main {

    // Plan d'expériences
    static final int[]    LAMBDAS = {4, 6, 8, 12};
    static final double   MU      = 8.0;
    static final double   DUREE   = 100_000.0;   // horizon de simulation (s)
    static final long     GRAINE  = 12345L;

    // (nom, nb_serveurs, capacite)  capacite = null -> infinie
    static final Object[][] MODELES = {
        {"M/M/1",   1, null},
        {"M/M/1/4", 1, 4},
        {"M/M/1/8", 1, 8},
        {"M/M/3/8", 3, 8},
    };

    static final Path RESULTS = Path.of("results");

    // ==================================================================
    //  1) testGateway
    // ==================================================================
    static void testGateway() {
        System.out.println("========================================");
        System.out.println(" testGateway()");
        System.out.println("========================================");

        // 1 serveur, capacité K=2 : on pourra observer service, file, rejet
        Gateway g = new Gateway(1, 2, MU, new java.util.Random(1));
        Message m1 = new Message(1, 1, 0);
        Message m2 = new Message(2, 1, 0);
        Message m3 = new Message(3, 1, 0);

        // m1 : serveur libre -> service immédiat (arrivée à t=1.0)
        Gateway.ServiceStart s1 = g.receive(m1, 1.0);
        g.printGateway();
        assert s1 != null && g.nbInSystem() == 1 && g.nbBusy() == 1 : "m1 devrait etre servi";
        assert Math.abs(m1.getArrivedTime() - 1.0) < 1e-9 : "arrivedTime non renseigne";

        // m2 : serveur occupé, système pas plein (1<2) -> file (arrivée à t=2.0)
        Gateway.ServiceStart s2 = g.receive(m2, 2.0);
        g.printGateway();
        assert s2 == null && g.nbInQueue() == 1 && g.nbInSystem() == 2 : "m2 devrait etre en file";

        // m3 : système plein (2>=2) -> rejet (arrivée à t=3.0)
        Gateway.ServiceStart s3 = g.receive(m3, 3.0);
        g.printGateway();
        assert s3 == null && g.getRejets() == 1 && g.nbInSystem() == 2 : "m3 devrait etre rejete";

        // fin de service de m1 -> on sert m2 (sorti de la file)
        Gateway.ServiceStart d1 = g.depart(m1);
        g.printGateway();
        assert d1 != null && d1.msg == m2 && g.nbInQueue() == 0 && g.nbInSystem() == 1
                : "le depart de m1 devrait declencher le service de m2";

        // fin de service de m2 -> file vide
        Gateway.ServiceStart d2 = g.depart(m2);
        g.printGateway();
        assert d2 == null && g.nbInSystem() == 0 : "systeme vide attendu";

        assert g.getArrivees() == 3 && g.getAcceptes() == 2
                && g.getRejets() == 1 && g.getDeparts() == 2 : "compteurs incoherents";
        System.out.println("→ testGateway : OK\n");
    }

    // ==================================================================
    //  2) testEngine
    // ==================================================================
    static void testEngine() throws IOException {
        System.out.println("========================================");
        System.out.println(" testEngine()  (simulation courte M/M/1)");
        System.out.println("========================================");

        Engine eng = new Engine(4, MU, 2000.0, 1, null, GRAINE);
        Map<String, Object> r = eng.run(RESULTS.resolve("trace_test.csv"), 25);

        System.out.printf(Locale.ROOT,
            "departs=%d  N_systeme=%.3f  W_systeme=%.4f  little_L=%.3f%n",
            (long) r.get("departs"), (double) r.get("N_systeme"),
            (double) r.get("W_systeme"), (double) r.get("little_L"));

        long departs = (long) r.get("departs");
        double Nsys = (double) r.get("N_systeme");
        double littleL = (double) r.get("little_L");
        assert departs > 0 : "la boucle n'a rien traite";
        // Loi de Little : little_L doit être proche de N_systeme
        assert Math.abs(littleL - Nsys) / Nsys < 0.10 : "loi de Little incoherente";
        assert Files.exists(RESULTS.resolve("trace_test.csv")) : "trace non generee";
        System.out.println("→ testEngine : OK (trace : results/trace_test.csv)\n");
    }

    // ==================================================================
    //  3) Campagne de simulations
    // ==================================================================
    static List<Map<String, Object>> simulations() {
        System.out.println("========================================");
        System.out.println(" CAMPAGNE DE SIMULATIONS (T=" + (long) DUREE + " s)");
        System.out.println("========================================");
        System.out.printf("%-8s %3s | %6s | %9s | %8s | %7s | %s%n",
                "modele", "lam", "rho", "N_sys", "W_sys", "rejet", "th_L");
        System.out.println("----------------------------------------------------------------");

        List<Map<String, Object>> lignes = new ArrayList<>();
        for (Object[] mod : MODELES) {
            String nom = (String) mod[0];
            int c = (int) mod[1];
            Integer K = (Integer) mod[2];
            for (int lam : LAMBDAS) {
                Engine eng = new Engine(lam, MU, DUREE, c, K, GRAINE);
                Map<String, Object> r = eng.run();
                ajouterTheorie(r, lam, c, K);
                lignes.add(r);

                System.out.printf(Locale.ROOT,
                    "%-8s %3d | %6.3f | %9.3f | %8.4f | %6.2f%% | %s%n",
                    nom, lam, (double) r.get("rho"), (double) r.get("N_systeme"),
                    (double) r.get("W_systeme"), (double) r.get("taux_rejet") * 100.0,
                    r.get("th_L"));
            }
        }
        return lignes;
    }

    /** Ajoute les valeurs théoriques (préfixe th_) à une ligne de résultat. */
    static void ajouterTheorie(Map<String, Object> r, int lam, int c, Integer K) {
        if (K == null) {
            Theorie.Resultat th = Theorie.mm1(lam, MU);
            if (!th.stable) {
                r.put("th_rho", round(th.rho)); r.put("th_L", "inf");
                r.put("th_Lq", "inf"); r.put("th_W", "inf");
                r.put("th_Wq", "inf"); r.put("th_rejet", 0.0);
            } else {
                r.put("th_rho", round(th.rho)); r.put("th_L", round(th.L));
                r.put("th_Lq", round(th.Lq)); r.put("th_W", round(th.W));
                r.put("th_Wq", round(th.Wq)); r.put("th_rejet", 0.0);
            }
        } else {
            Theorie.Resultat th = Theorie.mmck(lam, MU, c, K);
            r.put("th_rho", round(th.rho)); r.put("th_L", round(th.L));
            r.put("th_Lq", round(th.Lq)); r.put("th_W", round(th.W));
            r.put("th_Wq", round(th.Wq)); r.put("th_rejet", round(th.pRejet));
        }
    }

    static double round(double x) { return Math.round(x * 10000.0) / 10000.0; }

    // ------------------------------------------------------------------
    //  Comparaison sim vs théorie (affichage console)
    // ------------------------------------------------------------------
    static void comparaison(List<Map<String, Object>> lignes) {
        System.out.println("\n========================================");
        System.out.println(" COMPARAISON SIMULATION vs THEORIE");
        System.out.println("========================================");
        System.out.printf("%-8s %3s | %8s %8s | %8s %8s | %7s %7s%n",
                "modele", "lam", "N_sim", "L_th", "W_sim", "W_th", "rej_sim", "rej_th");
        System.out.println("---------------------------------------------------------------------");
        for (Map<String, Object> r : lignes) {
            String thRej = r.get("th_rejet") instanceof Double
                    ? String.format(Locale.ROOT, "%.2f", (double) r.get("th_rejet") * 100.0)
                    : String.valueOf(r.get("th_rejet"));
            System.out.printf(Locale.ROOT,
                "%-8s %3.0f | %8.3f %8s | %8.4f %8s | %6.2f%% %6s%%%n",
                r.get("modele"), (double) r.get("lambda"),
                (double) r.get("N_systeme"), String.valueOf(r.get("th_L")),
                (double) r.get("W_systeme"), String.valueOf(r.get("th_W")),
                (double) r.get("taux_rejet") * 100.0, thRej);
        }
    }

    // ------------------------------------------------------------------
    //  Écriture CSV des résultats
    // ------------------------------------------------------------------
    static final String[] COLONNES = {
        "modele","lambda","mu","serveurs","capacite","T","rho","arrivees",
        "acceptes","rejets","departs","taux_rejet","lambda_eff","N_systeme",
        "N_file","W_systeme","W_file","little_L","little_Lq",
        "th_rho","th_L","th_Lq","th_W","th_Wq","th_rejet"
    };

    static void ecrireCsv(List<Map<String, Object>> lignes) throws IOException {
        Files.createDirectories(RESULTS);
        Path chemin = RESULTS.resolve("resultats.csv");
        try (BufferedWriter w = Files.newBufferedWriter(chemin)) {
            w.write(String.join(",", COLONNES));
            w.write("\n");
            for (Map<String, Object> r : lignes) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < COLONNES.length; i++) {
                    if (i > 0) sb.append(",");
                    Object v = r.get(COLONNES[i]);
                    sb.append(formatVal(v));
                }
                w.write(sb.toString());
                w.write("\n");
            }
        }
        System.out.println("\nResultats ecrits dans " + chemin);
    }

    static String formatVal(Object v) {
        if (v == null) return "";
        if (v instanceof Double) return String.format(Locale.ROOT, "%.6g", (Double) v);
        return v.toString();
    }

    // ------------------------------------------------------------------
    //  Trace d'exemple (courte) + vérification chronologique
    // ------------------------------------------------------------------
    static void traceExemple() throws IOException {
        Engine eng = new Engine(4, MU, 100.0, 1, null, 7L);
        Path chemin = RESULTS.resolve("trace_exemple.csv");
        eng.run(chemin, 20);

        // Vérifie que les temps sont croissants (ordre chronologique)
        List<String> l = Files.readAllLines(chemin);
        double prec = -1.0;
        for (int i = 1; i < l.size(); i++) {                 // saute l'entête
            double t = Double.parseDouble(l.get(i).split(",")[0]);
            if (t < prec) throw new IllegalStateException("trace non chronologique !");
            prec = t;
        }
        System.out.println("\nTrace d'exemple : " + chemin
                + "  (" + (l.size() - 1) + " lignes, ordre chronologique OK)");
        // Affiche les premières lignes
        for (int i = 0; i < Math.min(l.size(), 9); i++) System.out.println("   " + l.get(i));
    }

    // ==================================================================
    //  main
    // ==================================================================
    public static void main(String[] args) throws IOException {
        testGateway();
        testEngine();

        List<Map<String, Object>> lignes = simulations();
        comparaison(lignes);
        ecrireCsv(lignes);
        traceExemple();

        System.out.println("\n========================================");
        System.out.println(" Seance 3 terminee : simulateur complet.");
        System.out.println("========================================");
    }
}
