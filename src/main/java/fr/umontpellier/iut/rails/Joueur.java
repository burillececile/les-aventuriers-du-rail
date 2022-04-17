package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    public static enum Couleur {    //Les couleurs possibles pour les joueurs (pour l'interface graphique)
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    private Jeu jeu;                        //Jeu auquel le joueur est rattaché
    private String nom;                     //Nom du joueur
    private Couleur couleur;                //CouleurWagon du joueur (pour représentation sur le plateau)
    private int nbGares;                    //Nombre de gares que le joueur peut encore poser sur le plateau
    private int nbWagons;                   //Nombre de wagons que le joueur peut encore poser sur le plateau
    private List<Destination> destinations; // Liste des missions à réaliser pendant la partie
    private List<CouleurWagon> cartesWagon; //Liste des cartes que le joueur a en main
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    private int score;                      //Score courant du joueur (somme des valeurs des routes capturées)
    private HashMap<Integer, Integer> pointLongueur = new HashMap<>();

    /**
     * On va initialiser un joueur à partir d'un nom, d'un jeu et d'une couleur
     * selon les règles, il commence avec 3 gares, 45 wagons. Son marqueur de score est initialisé à 12 (3 gares * 4 = 12).
     * On initialise aux joueurs une liste : cartesWagon, cartesWagonPosees, destinations
     * Ces dernières sont plutôt explicite quant à leur fonction. (liste vide qui contiendra les cartesWagons et destination de chaque joueur).
     */
    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
        pointLongueurInit();
    }

    public int getNbGares() {   //Ajouté pour les tests unitaires
        return nbGares;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */

    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {

        String choix = "1";
        ArrayList<Destination> destinationsDefausse = new ArrayList<>();

        while (!choix.equals("") && n < destinationsPossibles.size()) {

            ArrayList<String> listeBouton = new ArrayList<>();
            for (Destination destinationString : destinationsPossibles) {
                listeBouton.add(destinationString.getNom());
            }

            choix = jeu.getJoueurCourant().choisir(
                    "Vous pouvez vous défausser de 2 cartes destinations, mais le voulez vous ?",
                    new ArrayList<>(),  // choix (hors boutons, ici aucun)
                    listeBouton,        // boutons
                    true);              // le joueur peut passer

            if (!choix.equals("")) {
                destinationsDefausse.add(destinationsPossibles.get(listeBouton.indexOf(choix)));    //On add la destinationPossible à l'index de choix dans listeBouton sinon pb de type
                destinationsPossibles.remove(listeBouton.indexOf(choix));                           //On supprime la destination à l'index choix
            } else if (choix.equals("")) {
                log(nom + " Passe");
            }
        }

        this.destinations.addAll(destinationsPossibles);
        return destinationsDefausse;
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {
        ArrayList<CouleurWagon> choixWagons = CouleurWagon.getCouleursComplexe();    //Contient les couleurs en CouleurWagon
        ArrayList<String> choixWagonString = new ArrayList<>();                     //Contient les couleurs en String

        ArrayList<Ville> touteLesVilles = new ArrayList<>(jeu.getVilles());         //Contient les villes en Ville
        ArrayList<String> touteLesVillesString = new ArrayList<>();                 //Contient les villes en String

        ArrayList<Route> touteLesRoutes = new ArrayList<>(jeu.getRoutes());
        ArrayList<String> touteLesRouteString = new ArrayList<>();

        ArrayList<String> tousLesChoix = new ArrayList<>();                         //Contient toutes les commandes attendues

        for (CouleurWagon wagon : choixWagons) {    //Convertion des wagons en String
            choixWagonString.add(wagon.name());
        }

        for (Ville ville : touteLesVilles) {
            touteLesVillesString.add(ville.getNom());
        }

        for (Route route : touteLesRoutes) {
            touteLesRouteString.add(route.getNom());
        }

        tousLesChoix.add("GRIS");
        tousLesChoix.add("destinations");
        tousLesChoix.addAll(choixWagonString);
        tousLesChoix.addAll(touteLesVillesString);
        tousLesChoix.addAll(touteLesRouteString);

        String choixTourJoueur = choisir(
                "Que voulez vous faire ?",
                tousLesChoix, //Si on clique sur une CarteWagonVisible ou sur la pioche ("GRIS")
                new ArrayList<>(),
                true);

        //Si on cliquesur un wagon visible ou Locomotive oy gris alors methode

        if (choixWagonString.contains(choixTourJoueur) || choixTourJoueur.equals("GRIS")) {
            if (jeu.getCartesWagonVisibles().contains(CouleurWagon.valueOf(choixTourJoueur)) || choixTourJoueur.equals("GRIS")) {
                prendreCarteWagon(choixTourJoueur, choixWagonString);
            } else
                jouerTour();
        }
        else if (choixTourJoueur.equals("destinations")) {
            ArrayList<Destination> choixDestination = new ArrayList<>();
            int nbDestination = jeu.getPileDestinations().size();

            for (int i = 0; i < nbDestination && i < 3; i++) {
                choixDestination.add(jeu.piocherDestination());
            }

            ArrayList<Destination> defausseDestination = new ArrayList<>(choisirDestinations(choixDestination, 1));

            for (Destination destinationDefaussee : defausseDestination) {
                jeu.getPileDestinations().add(destinationDefaussee);
            }

            //if (jeu.getPileCartesWagon().isEmpty())   Pourquoi on avait ça ici ?
            //  jouerTour();

        } else if (touteLesVillesString.contains(choixTourJoueur)) {
            Ville villeSelectionne = touteLesVilles.get(touteLesVillesString.indexOf(choixTourJoueur));
            if (villeSelectionne.getProprietaire() == null && nbGares > 0) {    //Si la ville n'a pas de propriétaire et que le joueur a assez de gares alors :
                String message = "Choisissez une couleur pour votre gare";
                capturerGare(villeSelectionne, choixWagonString, message);
            } else  //Si pas assez de gare ou ville déjà prise alors le joueur rejoue
                jouerTour();

        } else if (touteLesRouteString.contains(choixTourJoueur)) {
            Route routeSelectionne = touteLesRoutes.get(touteLesRouteString.indexOf(choixTourJoueur));
            Route routeSelectionneParallel = null;
            boolean parallele = false;
            int coutEtVal = 0;

            for (Route route : touteLesRoutes) {
                if (route.getVille1() == routeSelectionne.getVille1() && route.getVille2() == routeSelectionne.getVille2() && !route.getNom().equals(routeSelectionne.getNom())) {
                    parallele = true;
                    routeSelectionneParallel = route;
                }
            }

            if (routeSelectionne.getLongueur() <= nbWagons) {       //Le joueur a assez de wagons
                if (routeSelectionne.getProprietaire() == null) {   //La route n'a pas de propriétaire
                    if (parallele) {
                        if (routeSelectionne.getProprietaire() != routeSelectionneParallel.getProprietaire() || (routeSelectionne.getProprietaire() == null && routeSelectionneParallel.getProprietaire() == null)) {
                            //Peut prendre le tunnel parallèle
                            coutEtVal = routeSelectionne.prendreRoute(this, choixWagonString);
                        } else {
                            log("Pas possible de prendre les 2 routes parallèles");
                            jouerTour();
                        }
                    } else {
                        coutEtVal = routeSelectionne.prendreRoute(this, choixWagonString);
                    }
                } else {
                    log("Route déjà prise");
                    jouerTour();
                }
            } else {
                log("Pas assez de wagons");
                jouerTour();
            }
            if (coutEtVal > 0) {
                nbWagons -= coutEtVal;
                score += pointLongueur.get(coutEtVal);
            } else if (coutEtVal == 0)
                jouerTour();
        }
    }

    public void defausseMainCoul(String couleur, int cout) {                       //Méthode qui prend en paramètre une couleur et un nombre max de carte à défausser
        int nbCarteDefausse = 0;
        for (int wagon = cartesWagon.size() - 1; wagon >= 0 && nbCarteDefausse < cout; wagon--) {    //Pour y inférieur à la taille de la main du joueur nbCarteDefausse && inférieur au nombre de cartes à défausser
            if (cartesWagon.get(wagon).name().equals(couleur)) {                        //Si la carte à y égale la couleur défaussé :
                jeu.defausserCarteWagon(cartesWagon.get(wagon));                        //On supprime la carte en question de la main du joueur
                cartesWagon.remove(wagon);
                nbCarteDefausse++;                                                  //On incrémente le nbCarteDefausse
            }
        }
    }

    public void poseesMainCoul(String couleur) {                             //Méthode qui va poser la première itération de la couleur fournit sans la retirer de la main du joueur
        for (int wagon = cartesWagon.size() - 1; wagon >= 0; wagon--) {
            if (cartesWagon.get(wagon).name().equals(couleur)) {
                cartesWagonPosees.add(cartesWagon.get(wagon));
                break;
            }
        }
    }

    public int nbCarteDeCouleur(String couleur) { //Méthode qui calcul le nombre de carte dans la main du joueur courant à partir d'une couleur en format String
        int nbCarteCoul = 0;
        for (CouleurWagon wagonMain : cartesWagon) {         //Regarder la copie de la main du joueur
            if (wagonMain.name().equals(couleur)) {          //Si il a une carte de la couleur sélectionné (choixCouleur)
                nbCarteCoul++;                               //Alors il incrémente son nb de carte de la couleur sélectionné
            }
        }
        return nbCarteCoul;
    }

    /**
     * Méthode qui calcul le nombre de cartes dans la main du joueurCourant (this) moins les cartes de ce même joueur
     * posées, afin de prendre une route par exemple.
     *
     * @param couleur au format string, sert à désigner la couleur à calculer
     * @return Le nombre de cartes, de la couleur fournit en paramètre, en int.
     */

    public int nbCarteDeCouleurMoinsPosee(String couleur) {

        //On créer une liste qui copie la main du joueur
        ArrayList<CouleurWagon> copieMainJoueur = new ArrayList<>(cartesWagon);

        int nbCarteCoul = 0;                                //On initialise le nombre de carte de la couleur fournit en paramètre
        for (CouleurWagon wagonPosee : cartesWagonPosees) { //On retire les cartes posées aux cartes en mains
            copieMainJoueur.remove(wagonPosee);
        }

        //Note : Il ne peut pas y avoir plus de cartes d'une couleur posées que de cartes en main, cela est vérifié par
        //le programme qui exécute cette méthode

        for (CouleurWagon wagonMain : copieMainJoueur) {    //Regarder la copie de la main du joueur
            if (wagonMain.name().equals(couleur)) {         //Si le joueur a une carte de la couleur en paramètre alors
                nbCarteCoul++;                              //On incrémente son nb de carte de la couleur sélectionné
            }
        }
        return nbCarteCoul;                                 //On retourne le nombre de cartes de la couleur
    }

    public void pointLongueurInit() {
        pointLongueur.put(1, 1);
        pointLongueur.put(2, 2);
        pointLongueur.put(3, 4);
        pointLongueur.put(4, 7);
        pointLongueur.put(6, 15);
        pointLongueur.put(8, 21);
    }

    public int getScore() {
        return score;
    }

    public void capturerGare(Ville villeSelectionne, ArrayList<String> choixWagonString, String message) {

        int cout = 3 - (nbGares - 1);       //1 carte pour 1 gare, 2 carte pour 2 gare ...
        int nbCarteCoul;                    //Nb carte de la couleur cherchée dans la main du joueur

        String choixCouleur = choisir(message,   //On lui propose de se débarasser d'un cout * nb couleur / locomotives dans sa main
                new ArrayList<>(),
                choixWagonString,
                true);

        if (!choixCouleur.equals("")) {  //S'il ne passe pas
            nbCarteCoul = nbCarteDeCouleur(choixCouleur);   //On retourne le nombre de carte de couleur dans la main du joueur

            if (nbCarteCoul >= cout) {                          //Si il a un nombre de carte sélectionné supérieur ou égale au cout alors :
                defausseMainCoul(choixCouleur, cout);
                villeSelectionne.setProprietaire(this);
                nbGares--;
                score -= 4;
            } else {                                             //Si il n'a pas assez de couleur alors il rejoue son tour
                String messageErreur = "Pas assez de carte de la couleur sélectionnez, choisissez une autre couleur";
                capturerGare(villeSelectionne, choixWagonString, messageErreur);
            }
        }
    }

    public void prendreCarteWagon(String choixTourJoueur, ArrayList<String> choixWagonString) {

        int nbAction = 2;

        while (nbAction > 0) {

            CouleurWagon choixTourJoueurWagon = CouleurWagon.valueOf(choixTourJoueur);

            if (jeu.getCartesWagonVisibles().contains(choixTourJoueurWagon)) {
                cartesWagon.add(choixTourJoueurWagon);
                if (choixTourJoueur.equals("LOCOMOTIVE"))
                    nbAction--;
                nbAction--;

                if (!jeu.getPileCartesWagon().isEmpty())    //Nécessaire car le return null return quand même une carte
                    jeu.retirerCarteWagonVisible(choixTourJoueurWagon);
                else
                    jeu.getCartesWagonVisibles().remove(choixTourJoueurWagon);
            } else if (choixTourJoueur.equals("GRIS")) {
                CouleurWagon pioche = jeu.piocherCarteWagon();
                nbAction--;
                if (pioche != null) {
                    cartesWagon.add(pioche);
                }
                else
                    jouerTour();
            }
            if (nbAction == 1) {
                choixWagonString.remove("LOCOMOTIVE");
                choixWagonString.add("GRIS");
                choixTourJoueur = choisir(
                        "Choisissez une deuxième carte à piocher",
                        choixWagonString,                       //On refait notre choix mais plus sur les Locomotives Visibles
                        new ArrayList<>(),
                        false);
            }


        }
    }


}

