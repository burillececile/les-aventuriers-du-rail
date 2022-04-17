package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.List;

public class Tunnel extends Route {
    public Tunnel(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        super(ville1, ville2, longueur, couleur);
    }

    @Override
    public String toString() {
        return "[" + super.toString() + "]";
    }

    @Override
    public int prendreRoute(Joueur joueurSelectionne, ArrayList<String> choixWagonString) {

        String couleurDefausse = getCouleur().name();
        if (getCouleur().name().equals("GRIS"))
            couleurDefausse = choixWagonPoseGris(joueurSelectionne, choixWagonString);

        couleurDefausse = choixWagonPose(joueurSelectionne, couleurDefausse);
        if (!couleurDefausse.equals(""))
            if (choixCartesTunnel(joueurSelectionne, couleurDefausse) == 1)
                return defausseWagonPose(joueurSelectionne);

        joueurSelectionne.getCartesWagonPosees().clear();
        return -1;
    }

    public int choixCartesTunnel(Joueur joueurSelectionne, String couleurDefausse) {

        //Etape de tirage
        StringBuilder affichageChoisir = new StringBuilder();
        int nbCarteSupp = 0;

        for (int piocheTunnel = 0; piocheTunnel < 3; piocheTunnel++) { //On pioche 3 cartes et pour chaques cartes
            if (couleurDefausse.equals("LOCOMOTIVE")) {                  //Si on veut que des locomotives alors
                if (couleurDefausse.equals(joueurSelectionne.getJeu().getPileCartesWagon().get(piocheTunnel).name())) {  //Si la carte est égale à la couleur
                    nbCarteSupp++;
                }
            } else {                                                    //Si on veut la couleur et des locomotives
                if (couleurDefausse.equals(joueurSelectionne.getJeu().getPileCartesWagon().get(piocheTunnel).name()) || "LOCOMOTIVE".equals(joueurSelectionne.getJeu().getPileCartesWagon().get(piocheTunnel).name())) {
                    nbCarteSupp++;
                }
            }
            joueurSelectionne.getJeu().defausserCarteWagon(joueurSelectionne.getJeu().getPileCartesWagon().get(piocheTunnel));
            affichageChoisir.append(" ").append(joueurSelectionne.getJeu().getPileCartesWagon().get(piocheTunnel).name());
        }

        joueurSelectionne.getJeu().getPileCartesWagon().subList(0, 2).clear();  //On supprime les 3 cartes wagons du haut de la pile.

        //Nouveau calcul de la route
        int nbCarteCoul = 0;

        if (!couleurDefausse.equals("LOCOMOTIVE"))
            nbCarteCoul = joueurSelectionne.nbCarteDeCouleurMoinsPosee(couleurDefausse);  //A faire sur la main moins les cartes posees
        int nbCarteLoco = joueurSelectionne.nbCarteDeCouleurMoinsPosee("LOCOMOTIVE");

        if (nbCarteLoco + nbCarteCoul >= nbCarteSupp) {
            for (int defausseSupp = 0; defausseSupp < nbCarteSupp; defausseSupp++) {

                ArrayList<String> couleurPossible = new ArrayList<>();
                if (nbCarteCoul > 0)
                    couleurPossible.add(couleurDefausse);
                if (nbCarteLoco > 0)
                    couleurPossible.add("LOCOMOTIVE");

                String poserCarteSupp = joueurSelectionne.choisir("Les nouveaux wagons sont : " + affichageChoisir + " Voulez vous dépenser " + nbCarteSupp + " Cartes en plus ? ",
                        new ArrayList<>(),
                        couleurPossible,
                        true);

                couleurPossible.clear();

                if (!poserCarteSupp.equals("")) {
                    joueurSelectionne.poseesMainCoul(poserCarteSupp);
                    if (poserCarteSupp.equals("LOCOMOTIVE")) {
                        nbCarteLoco--;
                    } else
                        nbCarteCoul--;
                } else {
                    return 0;
                }
            }
            return 1;
        }
        return 0;
    }


}
