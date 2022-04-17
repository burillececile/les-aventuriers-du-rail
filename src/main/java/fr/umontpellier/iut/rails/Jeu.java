package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.net.CookieHandler;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {

    private List<Joueur> joueurs;   //Liste des joueurs
    private Joueur joueurCourant;   //Le joueur dont c'est le tour
    private List<Ville> villes;     //Liste des villes représentées sur le plateau de jeu
    private List<Route> routes;     //Liste des routes du plateau de jeu
    private List<CouleurWagon> pileCartesWagon; //Pile de pioche (face cachée)
    private List<CouleurWagon> cartesWagonVisibles; //Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
    private List<CouleurWagon> defausseCartesWagon; //Pile de cartes qui ont été défaussée au cours de la partie
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    private BlockingQueue<String> inputQueue;   //File d'attente des instructions recues par le serveur
    private List<String> log;   //Messages d'information du jeu

    public Jeu(String[] nomJoueurs) {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         * 
         * Le code indiqué ici est un squelette minimum pour que le jeu se lance et que
         * l'interface graphique fonctionne.
         * Vous devez modifier ce code pour que les différents éléments du jeu soient
         * correctement initialisés.
         */

        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des cartes
        pileCartesWagon = CouleurWagon.makeWagonsEurope();          //La liste complète des cartes wagons + locomotives du jeu mélangé.
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        pileDestinations = Destination.makeDestinationsEurope();    //La pile de Destination = static Destination (toute les destinatiosn de l'europe).

        // création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));
            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        Collections.shuffle(pileCartesWagon);       //Mélange des cartes wagons
        Collections.shuffle(pileDestinations);      //Mélange des destinations

        //Distribution de 4 cartes par joueurs
        for(int ditribWagon = 0 ; ditribWagon < joueurs.size() ; ditribWagon++){
            for(int nbCarte = 0 ; nbCarte < 4 ; nbCarte++) {
                joueurs.get(ditribWagon).getCartesWagon().add(piocherCarteWagon()); //On rajoute une carte wagon à chaque joueur.
            }
        }
        log("Distribution de 4 cartes Wagons par joueurs");

        //Retournage des 5 cartes au dessus du paquet (face visible).
        for( int nbCarteVisible = cartesWagonVisibles.size(); nbCarteVisible<5; nbCarteVisible++){
            cartesWagonVisibles.add(piocherCarteWagon());   //On rajoute 5 cartes wagon à la face visible
        }
        log("Revelation des 5 Wagons à piocher");

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    public List<CouleurWagon> getDefausseCartesWagon() {    //Ajouté pour les test unitaires
        return defausseCartesWagon;
    }



    public void run() { //Exécute la partie
        /*
         * ATTENTION : Cette méthode est à réécrire.
         * 
         * Cette méthode doit :
         * - faire choisir à chaque joueur les destinations initiales qu'il souhaite
         * garder :
         * on pioche 3 destinations "courtes" et 1 destination "longue", puis
         * le joueur peut choisir des destinations à défausser ou passer s'il ne veut plus
         * en défausser. Il doit en garder au moins 2.
         *
         * - exécuter la boucle principale du jeu qui fait jouer le tour de chaque
         * joueur à tour de rôle jusqu'à ce qu'un des joueurs n'ait plus que 2 wagons ou
         * moins
         *
         * - exécuter encore un dernier tour de jeu pour chaque joueur après
         */

        //Distribution des cartes - Destinations longue (1 par joueur) - Destination courte (3 par joueur)
        ArrayList<Destination> destL = Destination.makeDestinationsLonguesEurope(); //On toque les destinations Longues
        Collections.shuffle(destL);
        for (Joueur joueur : joueurs) {
            ArrayList<Destination> destinationsTampon = new ArrayList<>();
            destinationsTampon.add(destL.get(joueurs.indexOf(joueur)));         //Pas la peine de les supprimer vu qu'elles ne seront plus utiles.
            for (int nbDestination = 0 ; nbDestination < 3 ; nbDestination++){  //Distribution des cartes courte cette fois (3 par joueurs).
                destinationsTampon.add(piocherDestination());
            }
            joueurCourant = joueurs.get(joueurs.indexOf(joueur));  //ptet pour un effet de style
            joueurs.get(joueurs.indexOf(joueur)).choisirDestinations(destinationsTampon, 2);
        }
        joueurCourant = joueurs.get(0);   //Si effet de style
        log("Distribution des 4 destinations");

        /*
        while(joueurCourant.getNbWagons() > 2) {
            for (Joueur joueur : joueurs) {
                joueurCourant = joueur;
                joueurCourant.jouerTour();
            }
        }
        log("On sort");*/

        /*
       while(joueurCourant.getNbWagons() >= 2) {
            for (int joueur = 0; joueur < joueurs.size() && joueurCourant.getNbWagons() >= 2; joueur++) {
                joueurCourant = joueurs.get(joueur);
                joueurCourant.jouerTour();
            }
        }
         */

        while(true) {
            for (Joueur joueur : joueurs) {
                joueurCourant = joueur;
                joueurCourant.jouerTour();

                if (joueurCourant.getNbWagons() <= 2) {
                    log("Dernier Tour");
                    ArrayList<Joueur> dernierTours = new ArrayList<>();

                    for (int finList = joueurs.indexOf(joueur) + 1 ; finList<joueurs.size() ; finList++ ) {
                        dernierTours.add(joueurs.get(finList));
                    }
                    for (int debutList = 0 ; debutList<= joueurs.indexOf(joueur) ; debutList++) {
                        dernierTours.add(joueurs.get(debutList));
                    }

                    for(Joueur joueurD : dernierTours) {
                        joueurCourant = joueurD;
                        joueurCourant.jouerTour();
                    }
                    System.out.println(estGagnant(this.joueurs).getNom());
                    log("Fin de partie");   //Le programme n'atteint jamais cette ligne ( s'arrête avant )@Disabled
                    return; //Pour arréter le programme
                }
            }
        }

        /*
        // Exemple d'utilisation
        while (true) {
            // le joueur doit choisir une valeur parmi "1", "2", "3", "4", "6" ou "8"
            // les choix possibles sont présentés sous forme de boutons cliquables
            String choix = joueurCourant.choisir(
                    "Choisissez une taille de route.", // instruction
                    new ArrayList<>(), // choix (hors boutons, ici aucun)
                    new ArrayList<>(Arrays.asList("1", "2", "3", "4", "6", "8")), // boutons
                    false); // le joueur ne peut pas passer (il doit faire un choix)

            // une fois la longueur choisie, on filtre les routes pour ne garder que les
            // routes de la longueur choisie
            int longueurRoute = Integer.parseInt(choix);
            ArrayList<String> routesPossibles = new ArrayList<>();
            for (Route route : routes) {
                if (route.getLongueur() == longueurRoute) {
                    routesPossibles.add(route.getNom());
                }
            }

            // le joueur doit maintenant choisir une route de la longueur choisie (les
            // autres ne sont pas acceptées). Le joueur peut choisir de passer (aucun choix)
            String choixRoute = joueurCourant.choisir(
                    "Choisissez une route de longueur " + longueurRoute, // instruction
                    routesPossibles, // choix (pas des boutons, il faut cliquer sur la carte)
                    new ArrayList<>(), // boutons (ici aucun bouton créé)
                    true); // le joueur peut passer sans faire de choix
            if (choixRoute.equals("")) {
                // le joueur n'a pas fait de choix (cliqué sur le bouton "passer")
                log("Auncune route n'a été choisie");
            } else {
                // le joueur a choisi une route
                log("Vous avez choisi la route " + choixRoute);
            }
        }
        */
    }

    /**
     * Ajoute une carte dans la pile de défausse.
     * Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
     * (parce que la pioche
     * et la défausse sont vides), alors il faut immédiatement rendre cette carte
     * face visible.
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {
        //joueurCourant.getCartesWagon().remove(c);   //On retire la carte au joueur  Les méthodes de la classe jeu ne doivent pas venir interférer avec joueur ?
        defausseCartesWagon.add(c);                 //On rajoute la carte au-dessus de la pile de défausse
        if(cartesWagonVisibles.size() < 5) {        //S'il y a moins de 5 cartes visibles
            for( int nbCartesVisibles = cartesWagonVisibles.size(); nbCartesVisibles < 5 && defausseCartesWagon.size() > 0; nbCartesVisibles++){ //Si il y a des cartes dans la défausse et qu'il n'y a pas 5 cartes visibles
                Collections.shuffle(defausseCartesWagon);   //On mélange la pile de carte defausse (nécessaire ?)
                cartesWagonVisibles.add(c);                 //On rend visible la carte
                defausseCartesWagon.remove(c);              //On la défausse de la défausse
            }
        }
    }

    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {
        if(!pileCartesWagon.isEmpty()){  //Si la pioche n'est pas vide alors :
            CouleurWagon cartePioche = pileCartesWagon.get(0); //On vient piocher la première carte du deck
            pileCartesWagon.remove(0);  //On le retire du dessus du deck
            return cartePioche;
        } else {                        //Sinon
            pileCartesWagon.addAll(defausseCartesWagon);    //On rajoute toute la défausse à pileCarteWagon
            defausseCartesWagon.clear();                    //On vide la défausse
            Collections.shuffle(pileCartesWagon);           //On mélange
            if(!pileCartesWagon.isEmpty()) {
                CouleurWagon cartePioche = pileCartesWagon.get(0); //On tire
                pileCartesWagon.remove(0);
                return cartePioche;
            }
            else
                return null;
        }
    }

    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c) {
        cartesWagonVisibles.remove(c);
        cartesWagonVisibles.add(piocherCarteWagon());

        int nbLocoVisibles = verifCarteWagonVisible();

        while (nbLocoVisibles >= 3 && pileCartesWagon.size() >= verifCarteWagonPile() + 1) {
            pileCartesWagon.addAll(cartesWagonVisibles);
            cartesWagonVisibles.clear();
            Collections.shuffle(pileCartesWagon);
            log("3 cartes Wagons ou plus");
            log("Remise en jeu des cartes visibles");
            int pioche = 0;
            while (pioche < 5) {
                cartesWagonVisibles.add(piocherCarteWagon());
                pioche++;
            }
            nbLocoVisibles = verifCarteWagonVisible();
        }
    }

    public int verifCarteWagonVisible() {
        int nbLocoVisibles = 0;
        for (CouleurWagon wagonVisibles : cartesWagonVisibles) {
            if(wagonVisibles.name().equals("LOCOMOTIVE"))
                nbLocoVisibles++;
        }
        return nbLocoVisibles;
    }

    public int verifCarteWagonPile() {
        int locoTotale = 0;
        for (CouleurWagon wagonPile : pileCartesWagon) {
            if(wagonPile.name().equals("LOCOMOTIVE"))
                locoTotale++;
        }
        return locoTotale;
    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     * 
     * @return la destination qui a été piochée (ou `null` si aucune destination
     *         disponible)
     */
    public Destination piocherDestination() {
        if(!pileDestinations.isEmpty()) {
            Destination destinationPioche = pileDestinations.get(0);
            pileDestinations.remove(0);
            return destinationPioche;
        } else
            return null;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     *         file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }

    public Joueur estGagnant(List<Joueur> joueurs){
        Joueur joueurGagnant = null;
        int score =0;
        for(Joueur joueur : joueurs){

            if(joueur.getScore() > score){
                score = joueur.getScore();
                joueurGagnant = joueur;
            } else if(joueur.getScore() == score){
                if (joueurGagnant.getNbGares() > joueur.getNbGares())
                    joueurGagnant = joueur;
            }
        }

        return joueurGagnant;
    }
}
