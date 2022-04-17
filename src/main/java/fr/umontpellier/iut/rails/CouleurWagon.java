package fr.umontpellier.iut.rails;

import java.util.*;

/**
 * Représentation des couleurs du jeu utilisées pour les cartes wagon et les joueurs
 */
public enum CouleurWagon {
    NOIR, BLANC, JAUNE, ROUGE, ORANGE, BLEU, VERT, ROSE, GRIS, LOCOMOTIVE;

    @Override
    public String toString() {
        return switch (this) {
            case NOIR -> "Noir";
            case BLANC -> "Blanc";
            case JAUNE -> "Jaune";
            case ROUGE -> "Rouge";
            case ORANGE -> "Orange";
            case BLEU -> "Bleu";
            case VERT -> "Vert";
            case ROSE -> "Rose";
            case GRIS -> "Gris"; // représente une couleur indéterminée
            case LOCOMOTIVE -> "Locomotive"; // peut remplacer n'importe quelle couleur
        };
    }

    public String toLog() {
        return String.format("<img class=\"couleur\" src=\"images/symbole-%s.png\"><span class=\"couleur %s\">%s</span>", name(), name().toLowerCase(), this);
    }

    /**
     * Renvoie la liste des couleurs "simples" c'est-à-dire sans LOCOMOTIVE ni GRIS
     */
    public static ArrayList<CouleurWagon> getCouleursSimples() {
        return new ArrayList<>(List.of(NOIR, BLANC, JAUNE, ROUGE, ORANGE, BLEU, VERT, ROSE));
    }

    public static ArrayList<CouleurWagon> getCouleursComplexe() {
        ArrayList<CouleurWagon> couleurComplexe = getCouleursSimples();
        couleurComplexe.add(LOCOMOTIVE);
        return couleurComplexe;
    }

    /**
     * Renvoie la représentation sous forme d'une chaîne de caractères d'une liste
     * non ordonnée de couleurs.
     * 
     * La chaîne est constituée du nom de chaque couleur qui apparaît dans la liste,
     * suivie éventuellement d'une chaîne de la forme " x n" où n est le nombre de
     * fois que la couleur apparaît dans la liste, si n > 1. Les couleurs sont
     * séparées par des virgules.
     * 
     * @param liste une liste de couleurs (considérée comme non ordonnée)
     * @return une chaîne de caractères décrivant les éléments qui apparaissent dans
     *         la liste
     */
    public static String listToString(List<CouleurWagon> liste) {
        StringJoiner joiner = new StringJoiner(", ");
        for (CouleurWagon c : CouleurWagon.values()) {
            int count = Collections.frequency(liste, c);
            if (count == 1) {
                joiner.add(c.toString());
            } else if (count > 1) {
                joiner.add(c.toString() + " x" + count);
            }
        }
        return joiner.toString();
    }

    public static String listToLog(List<CouleurWagon> liste) {
        StringJoiner joiner = new StringJoiner(", ");
        for (CouleurWagon c : CouleurWagon.values()) {
            int count = Collections.frequency(liste, c);
            if (count == 1) {
                joiner.add(c.toLog());
            } else if (count > 1) {
                joiner.add(c.toLog() + " x" + count);
            }
        }
        return joiner.toString();
    }

    public static ArrayList<CouleurWagon> makeWagonsEurope() { //@return une liste contenant tous les wagons de l'Europe
        ArrayList<CouleurWagon> wagons = new ArrayList<>();

        for (int wagonnets = 0; wagonnets < getCouleursSimples().size() ; wagonnets++){     //Pour chaques couleurs
            for (int wagonnetsCouleurs = 0; wagonnetsCouleurs < 12 ; wagonnetsCouleurs++){  //On créer 12 wagons de la couleur
                wagons.add(getCouleursSimples().get(wagonnets));
                //System.out.print(getCouleursSimples().get(wagonnets) + " " + wagonnetsCouleurs);      //Ligne de debug
            }
            //System.out.println("\n");     //Ligne de debug
        }
        for (int loco = 0 ; loco < 14 ; loco++){    //On créer 14 locomotives.
            wagons.add(LOCOMOTIVE);
        }
        return wagons;
    }

}
