package fr.umontpellier.iut.rails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JoueurTest {
    private IOJeu jeu;
    private Joueur joueur1;
    private Joueur joueur2;
    private Joueur joueur3;
    private Joueur joueur4;

    public Route getRouteParNom(String nom) {   //Copie de la méthode getRouteParNom dans JoueurProfTest.java
        for (Route route : jeu.getRoutes()) {
            if (route.getNom().equals(nom)) {
                return route;
            }
        }
        return null;
    }

    @BeforeEach     //Avant chaques tests, On initialise 4 joueurs et on clear leurs cartesWagon en main.
    void init() {
        jeu = new IOJeu(new String[] { "Guybrush", "Largo", "LeChuck", "Elaine" });
        List<Joueur> joueurs = jeu.getJoueurs();
        joueur1 = joueurs.get(0);
        joueur2 = joueurs.get(1);
        joueur3 = joueurs.get(2);
        joueur4 = joueurs.get(3);
        joueur1.getCartesWagon().clear();
        joueur2.getCartesWagon().clear();
        joueur3.getCartesWagon().clear();
        joueur4.getCartesWagon().clear();
    }


    @Test
    void testChoisirDestinations() {
        jeu.setInput("Athina - Angora (5)", "Frankfurt - Kobenhavn (5)");
        ArrayList<Destination> destinationsPossibles = new ArrayList<>();
        Destination d1 = new Destination("Athina", "Angora", 5);
        Destination d2 = new Destination("Budapest", "Sofia", 5);
        Destination d3 = new Destination("Frankfurt", "Kobenhavn", 5);
        Destination d4 = new Destination("Rostov", "Erzurum", 5);
        destinationsPossibles.add(d1);
        destinationsPossibles.add(d2);
        destinationsPossibles.add(d3);
        destinationsPossibles.add(d4);

        List<Destination> destinationsDefaussees = joueur1.choisirDestinations(destinationsPossibles, 2);
        assertEquals(2, joueur1.getDestinations().size());
        assertEquals(2, destinationsDefaussees.size());
        assertTrue(destinationsDefaussees.contains(d1));
        assertTrue(destinationsDefaussees.contains(d3));
        assertTrue(joueur1.getDestinations().contains(d2));
        assertTrue(joueur1.getDestinations().contains(d4));
    }

    @Test
    void testJouerTourPrendreCartesWagon() {
        jeu.setInput("GRIS", "ROUGE");

        // On met 5 cartes ROUGE dans les cartes wagon visibles
        List<CouleurWagon> cartesWagonVisibles = jeu.getCartesWagonVisibles();
        cartesWagonVisibles.clear();
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);

        // On met VERT, BLEU, LOCOMOTIVE (haut de pile) dans la pile de cartes wagon
        List<CouleurWagon> pileCartesWagon = jeu.getPileCartesWagon();
        pileCartesWagon.add(0, CouleurWagon.BLEU);
        pileCartesWagon.add(0, CouleurWagon.LOCOMOTIVE);
        int nbCartesWagon = pileCartesWagon.size();

        joueur1.jouerTour();
        // le joueur devrait piocher la LOCOMOTIVE, prendre une carte ROUGE
        // puis le jeu devrait remettre une carte visible BLEU

        assertTrue(TestUtils.contientExactement(
            joueur1.getCartesWagon(),
            CouleurWagon.ROUGE,
            CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(
                cartesWagonVisibles,
                CouleurWagon.BLEU,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE));
        assertEquals(nbCartesWagon - 2, pileCartesWagon.size());
    }

    @Test
    void testCapturerRouteCouleurEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Bruxelles - Frankfurt",    //Veut s'emparer de la route (coute 2 bleu).
                "LOCOMOTIVE",               //Utilise une Locomotive, ok
                "ROUGE",                    //à du rouge, mais ne convient pas pour la route.
                "BLEU"                      //Prend la route
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Bruxelles - Frankfurt").getProprietaire());   //Il a bien prit la route
        assertTrue(TestUtils.contientExactement(                                            //Il a défaussé le bon nombre de cartes
                joueur2.getCartesWagon(),
                CouleurWagon.BLEU, CouleurWagon.ROUGE, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(                                            //Elles sont bien dans la defausse
                jeu.getDefausseCartesWagon(),
                CouleurWagon.BLEU,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(                                            //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testPrendreRouteSeulementLocomotivesEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Bruxelles - Frankfurt",    //Veut prendre une route de 2 bleus
                "LOCOMOTIVE",               //Dépense 2 locomotives
                "LOCOMOTIVE"
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Bruxelles - Frankfurt").getProprietaire());   //Doit s'être emparé de la route
        assertTrue(TestUtils.contientExactement(                                            //Il a bien défaussé les 2 locomotives
                joueur2.getCartesWagon(),
                CouleurWagon.BLEU, CouleurWagon.ROUGE, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(                                            //Elles sont bien dans la défausse
                jeu.getDefausseCartesWagon(),
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(                                            //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerTunnelPossibleEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        //Le joueur piochera ces cartes après la première étape de prise du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.LOCOMOTIVE);   //1 wagon en plus
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);         //1 wagon en plus (total de 2)
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);        //Pas de wangon à rajouter

        jeu.setInput(
                "Marseille - Zurich",   //coût de 2 ROSE initial (tunnel)
                "LOCOMOTIVE",           //1 Locomotive, ok
                "ROSE",                 //1 Rose ok
                "LOCOMOTIVE",           //Cout augmenté de 2, utilise 1 Locomotive
                "LOCOMOTIVE"            //Utilise sa dernière Locomotive
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Marseille - Zurich").getProprietaire());  //À bien pris la route
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),   //Doit contenir les cartes du joueur ainsi que les 3 cartes piochées pour le tunnel
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.ROSE,
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.ROSE,
                CouleurWagon.JAUNE,
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(     //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerTunnelCouleurNonProposee() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        //Le joueur piochera ces cartes après la première étape de prise du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);     //1 bleu, ne modifie pas le cout
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);     //1 Rose, augmente le cout de 1
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);    //1 Jaune, ne modifie pas le cout

        jeu.setInput(
                "Marseille - Zurich",           //Tunnel d'un cout de 2 Rose / Loco
                "ROSE",                         //Dépense 1 ROSE, ok
                "LOCOMOTIVE",                   //Dépense 1 Locomotive, ok
                "ROUGE",                        //Essaye de dépenser une couleur non proposée.
                "ROSE"                          //Paye finalement la bonne carte.
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Marseille - Zurich").getProprietaire());  //Même vérification que pour Tunnel
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROUGE, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROSE,
                CouleurWagon.ROSE,
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.BLEU,
                CouleurWagon.ROSE,
                CouleurWagon.JAUNE));
        assertTrue(TestUtils.contientExactement(      //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerTunnelPasseEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        //Le joueur piochera ces cartes après la première étape de prise du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);     //1 bleu, ne modifie pas le cout
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);     //1 Rose, augmente le cout de 1
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);    //1 Jaune, ne modifie pas le cout

        jeu.setInput(
                "Marseille - Zurich",           //Tunnel d'un cout de 2 Rose / Loco
                "ROSE",                         //Dépense 1 ROSE, ok
                "LOCOMOTIVE",                   //Dépense 1 Locomotive, ok
                "");                            //Le joueur passe
        joueur2.jouerTour();
        assertEquals(null, getRouteParNom("Marseille - Zurich").getProprietaire());  //Le propriétaire est null
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROSE, CouleurWagon.ROUGE, CouleurWagon.ROUGE, CouleurWagon.LOCOMOTIVE));   //Il a toujours ses cartes en main
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),           //On retrouve les 3 cartes piochées
                CouleurWagon.ROSE,
                CouleurWagon.BLEU,
                CouleurWagon.JAUNE));
        assertTrue(TestUtils.contientExactement(        //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerTunnelSeulementLocomotiveEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        //Le joueur piochera ces cartes après la première étape de prise du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);     //Aucune cartes bonus car aucune locomotive
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);

        jeu.setInput(
                "Marseille - Zurich",           //Tunnel d'un cout de 2 Rose / Loco
                "LOCOMOTIVE",                   //Dépense 1 Locomotive, ok
                "LOCOMOTIVE");                  //Dépense 1 Locomotive, ok

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Marseille - Zurich").getProprietaire());                  //Le joueur est propriétaire
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROSE, CouleurWagon.ROUGE, CouleurWagon.ROUGE, CouleurWagon.LOCOMOTIVE));   //Les cartes qu'il a en main
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),           //On retrouve les 3 cartes piochées + Les 2 Loco
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.ROSE,
                CouleurWagon.BLEU,
                CouleurWagon.JAUNE));
        assertTrue(TestUtils.contientExactement(        //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerFerryEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Athina - Smyrna",              //Ferry d'un cout de 2 Gris / Loco
                "LOCOMOTIVE",                   //Dépense 1 Locomotive, ok
                "LOCOMOTIVE",                   //Dépense 1 Locomotive, pas possible, car plus assez en main
                "ROSE");                        //Dépense 1 Locomotive, ok

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Athina - Smyrna").getProprietaire());                  //Le joueur est propriétaire
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROUGE));                   //Les cartes qu'il a en main
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),           //On retrouve 1 Loco + 1 carte Rose
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.ROSE));
        assertTrue(TestUtils.contientExactement(        //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerFerry2Etudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Athina - Smyrna",              //Ferry d'un cout de 2 Gris / Loco
                "ROSE",
                "ROUGE",
                "LOCOMOTIVE");

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Athina - Smyrna").getProprietaire());                  //Le joueur est propriétaire
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROUGE));                   //Les cartes qu'il a en main
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),           //On retrouve 1 Loco + 1 carte Rose
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.ROSE));
        assertTrue(TestUtils.contientExactement(        //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testCapturerFerryPasseEtudiant() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Athina - Smyrna",              //Ferry d'un cout de 2 Gris / Loco
                "ROSE",
                "",                             //On passe la capture du ferry
                "");                            //Comme on peut encore jouer, on passe notre tour

        joueur2.jouerTour();
        assertEquals(null, getRouteParNom("Athina - Smyrna").getProprietaire());                  //Le joueur est propriétaire
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROSE, CouleurWagon.ROUGE, CouleurWagon.LOCOMOTIVE));                   //Les cartes qu'il a en main
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon()           //On retrouve 1 Loco + 1 carte Rose
                ));
        assertTrue(TestUtils.contientExactement(        //Il n'a plus de cartes posées
                joueur2.getCartesWagonPosees()
        ));
    }

    @Test
    void testgagnant (){

    }


}
