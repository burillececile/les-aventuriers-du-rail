package fr.umontpellier.iut.rails;

import java.util.ArrayList;

public class Ferry extends Route {
    /**
     * Nombre de locomotives qu'un joueur doit payer pour capturer le ferry
     */
    private int nbLocomotives;

    public Ferry(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur, int nbLocomotives) {
        super(ville1, ville2, longueur, couleur);
        this.nbLocomotives = nbLocomotives;
    }

    public int getNbLocomotives() {
        return nbLocomotives;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s, %d)]", getVille1(), getVille2(), getLongueur(), getCouleur(),
                nbLocomotives);
    }

    @Override
    public int prendreRoute(Joueur joueurSelectionne, ArrayList<String> choixWagonString) {     //Méthode qui permet de prendre une route en fonction de la route selectionné
        String couleurDefausse;
        if (joueurSelectionne.nbCarteDeCouleur("LOCOMOTIVE") >= getNbLocomotives()) { //Si il a le nombre de locomotives suffisantes
            couleurDefausse = choixWagonPoseGris(joueurSelectionne, choixWagonString); //On appel choixWagonPoseGros de Route
            if (couleurDefausse.equals("")) {
                joueurSelectionne.getCartesWagonPosees().clear();
                return 0;
            }
            if (!couleurDefausse.equals("LOCOMOTIVE"))  //S'il ne s'est pas emparé de la route seulement avec des locomotives.
                couleurDefausse = choixCartesFerry(joueurSelectionne, couleurDefausse);

            if (!couleurDefausse.equals(""))
                return defausseWagonPose(joueurSelectionne);
        }
        joueurSelectionne.getCartesWagonPosees().clear();
        return 0;
    }

    /**
     * Méthode qui, après le choix d'une couleur, propose à l'utilisateur de défausser dans l'ordre qu'il le souhaite la couleur
     * fournit en paramètre ainsi que le nombre de locomotives.
     * Cette méthode doit prendre en compte l'étape de choix de couleur précédente où le joueur a pu dépenser des locomotives.
     * Elle n'a pas besoin de vérifier si le nombre de locomotives ou le nombre de cartes de couleur est suffisant afin
     * de prendre ladite route, car réalisé au préalable.
     *
     * @param joueurSelectionne le joueur selectionné / joueur Courant
     * @param couleurDefausse La couleur choisit par l'utilisateur
     * @return On return un String notifiant de si le joueur a passé son tour ou non.
     */
    public String choixCartesFerry(Joueur joueurSelectionne, String couleurDefausse) {

        ArrayList<String> choixCouleur = new ArrayList<>();
        int nbCarteCoul = joueurSelectionne.nbCarteDeCouleurMoinsPosee(couleurDefausse);
        int nbCarteLoco = joueurSelectionne.nbCarteDeCouleurMoinsPosee("LOCOMOTIVE");

        //Le nombre de cartes de couleur dépensés
        int nbCarteLocoDefausse = joueurSelectionne.nbCarteDeCouleur("LOCOMOTIVE") - nbCarteLoco;
        int nbCarteCoulDefausse = joueurSelectionne.nbCarteDeCouleur(couleurDefausse) - nbCarteCoul;

        if (nbCarteCoulDefausse + nbCarteLocoDefausse < getLongueur()) {
            //S'il a assez de cartes de couleur et qu'il y a encore de la place pour poser des cartes de couleurs alors
            if (nbCarteCoul > 0 && nbCarteCoulDefausse < getLongueur() - getNbLocomotives())
                choixCouleur.add(couleurDefausse);
            if (nbCarteLoco > 0)
                choixCouleur.add("LOCOMOTIVE");

            String choix = joueurSelectionne.choisir("Choisir la carte à dépenser pour prendre le Ferry",
                    new ArrayList<>(),
                    choixCouleur,
                    true);

            if (choix.equals(""))
                return "";

            joueurSelectionne.poseesMainCoul(choix);
            return choixCartesFerry(joueurSelectionne, couleurDefausse);
        }
        return couleurDefausse;
    }
}


