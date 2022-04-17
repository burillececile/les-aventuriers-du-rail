package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Route {
    /**
     * Première extrémité
     */
    private Ville ville1;
    /**
     * Deuxième extrémité
     */
    private Ville ville2;
    /**
     * Nombre de segments
     */
    private int longueur;
    /**
     * CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
     */
    private CouleurWagon couleur;
    /**
     * Joueur qui a capturé la route (`null` si la route est encore à prendre)
     */
    private Joueur proprietaire;
    /**
     * Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles
     * (voir la classe Plateau pour plus de clarté)
     */
    private String nom;

    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;
    }

    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    /**
     * @return un objet simple représentant les informations de la route
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    public int prendreRoute(Joueur joueurSelectionne, ArrayList<String> choixWagonString) {

        String couleurDefausse = getCouleur().name();
        if (getCouleur().name().equals("GRIS")) {
            couleurDefausse = choixWagonPoseGris(joueurSelectionne, choixWagonString);
        }

        if (!choixWagonPose(joueurSelectionne, couleurDefausse).equals(""))
            return defausseWagonPose(joueurSelectionne);

        joueurSelectionne.getCartesWagonPosees().clear();
        return 0;
    }

    /**
     * Méthode qui, à partir d'une couleur donnée en paramètre, demande au joueur de faire un choix de cartes à défausser
     * afin de prendre ladite route.
     *
     * @param joueurSelectionne le joueur selectionné (JoueurCourant)
     * @param couleurDefausse la couleur fournit en paramètre, égale à la couleur de la route ou si grise à la couleur
     *                        que le joueur a choisit.
     * @return return une chaine de caractère étant soit la couleur que fournit en paramètre ou une chaine vide si le joueur
     * a passé son tour.
     */

    public String choixWagonPose(Joueur joueurSelectionne, String couleurDefausse) {
        int nbCarteCoul = 0;
        int nbCarteLoco;

        if (!couleurDefausse.equals("LOCOMOTIVE"))
            nbCarteCoul = joueurSelectionne.nbCarteDeCouleurMoinsPosee(couleurDefausse);  //On vient calculer les cartes de couleurs
        nbCarteLoco = joueurSelectionne.nbCarteDeCouleurMoinsPosee("LOCOMOTIVE");
        int nbCarteDefausse = joueurSelectionne.getCartesWagonPosees().size();

        if (nbCarteCoul + nbCarteLoco + nbCarteDefausse >= getLongueur()) {   //Si on a le nombre de cartes nécessaire alors
            //Le joueur se défausse des cartes dans l'ordre qu'il veut
            while (nbCarteDefausse < getLongueur()) {
                ArrayList<String> couleurPossible = new ArrayList<>();
                if (nbCarteCoul > 0)
                    couleurPossible.add(couleurDefausse);
                if (nbCarteLoco > 0)
                    couleurPossible.add("LOCOMOTIVE");

                String couleurDefausseTampon;
                couleurDefausseTampon = joueurSelectionne.choisir("Choisir une couleur de wagon pour prendre la route grise",     //Il choisit une couleur de defausse
                        new ArrayList<>(),
                        couleurPossible,
                        true);

                couleurPossible.clear();

                if (couleurDefausseTampon.equals("")) {      //S'il passe
                    return "";                                           //On sort
                }

                joueurSelectionne.poseesMainCoul(couleurDefausseTampon);    //Sinon on ajoute la carte de couleur à sa pose

                if (couleurDefausseTampon.equals("LOCOMOTIVE"))             //Si c'est une locomotive
                    nbCarteLoco--;                                          //On réduit son nombre de cartes locomotives
                else if (couleurDefausseTampon.equals(couleurDefausse))     //Si c'est une carte de couleur
                    nbCarteCoul--;                                          //On réduit son nombre de cartes de couleurs
                nbCarteDefausse = joueurSelectionne.getCartesWagonPosees().size();
            }
            //Une fois la boucle finie alors le joueur a dépensé les cartes nécessaires à la prise de la route
            //Si nous n'avons dépensé que des Locomotives alors :
            if(!joueurSelectionne.getCartesWagonPosees().contains(CouleurWagon.valueOf(couleurDefausse)))
                return "LOCOMOTIVE";    //On return LOCOMOTIVE
            return couleurDefausse;     //Sinon, on return la couleur de la route
        }
        return "";
    }


    /**
     * Méthode qui, si l'on clique sur une route grise, demande au joueur de choisir une couleur pour cette route.
     * S'il ne choisit pas de couleur alors il retourne LOCOMOTIVE.
     * S'il choisit une couleur alors on vérifie la taille de la route pour savoir s'il a bien choisi la bonne couleur pour prendre la route.
     * Dans le cas contraire, le joueur, devra re-sélectionner une couleur.
     *
     * @param joueurSelectionne Le joueur sélectionné / le joueur Courant
     * @param choixWagonString  La liste des choix de couleurs proposé dans la classe joueur, contenant tout les choix de couleurs
     * @return on retourne la couleur sélectionnée valide / LOCOMOTIVE si on s'empart de la route seulement avec des LOCOMOTIVES
     *  ou "" si on passe notre tour.
     */
    public String choixWagonPoseGris(Joueur joueurSelectionne, ArrayList<String> choixWagonString) {   //La route est grise

        int nbCarteDefausse = joueurSelectionne.getCartesWagonPosees().size();          //On calcul le nombre de cartes défaussés
        int nbCarteLoco = joueurSelectionne.nbCarteDeCouleurMoinsPosee("LOCOMOTIVE");   //On calcul le nombre de cartes locomotives en main

        if (nbCarteLoco > 0 && !choixWagonString.contains("LOCOMOTIVE"))                 //S'il est supérieur à 0 alors et pas déjà présent
            choixWagonString.add("LOCOMOTIVE");                                          //On propose le chois locomotive

        if (getLongueur() > nbCarteDefausse) {  //Si on n'a pas encore défaussé de cartes
            String couleurDefausse = joueurSelectionne.choisir("Choisir une couleur de wagon pour prendre la route grise",     //Il choisit une couleur de défausser
                    new ArrayList<>(),
                    choixWagonString,
                    true);

            if (couleurDefausse.equals("LOCOMOTIVE")) {                            //S'il choisit une locomotive
                joueurSelectionne.poseesMainCoul(couleurDefausse);                 //On l'ajoute aux cartes posées
                choixWagonString.remove("LOCOMOTIVE");                          //On retire la proposition LOCOMOTIVE
                return choixWagonPoseGris(joueurSelectionne, choixWagonString);    //On rappelle la méthode
            } else if (couleurDefausse.equals("")) {
                return "";
            }                                                                      //S'il choisit une couleur alors
            int nbCarteCoul = joueurSelectionne.nbCarteDeCouleurMoinsPosee(couleurDefausse);    //On calcul les de cette couleur qu'il a en main
            if (nbCarteCoul + nbCarteLoco + nbCarteDefausse >= getLongueur()) {                 //S'il en a suffisamment
                joueurSelectionne.poseesMainCoul(couleurDefausse);                              //On pose la couleur qu'il a sélectionné
                return couleurDefausse;                                                         //On retourne sa couleur
            } else
                return choixWagonPoseGris(joueurSelectionne, choixWagonString);                 //Sinon on lui demande de refaire un choix
        }                                       //Sinon on return LOCOMOTIVE
        return "LOCOMOTIVE";
    }

    /**
     * Défausse les cartes de la variable carteWagon d'un joueur donné en paramètre en fonction de ses cartesWagonPosée.
     * Return ensuite la longueur de la route pour laquelle on a défaussé ces cartes.
     *
     * @param joueurSelectionne qui appel la méthode.
     * @return getLongueur();
     */
    public int defausseWagonPose(Joueur joueurSelectionne) {
        for (CouleurWagon wagon : joueurSelectionne.getCartesWagonPosees()) {    //Pour chacun des wagons posés
            joueurSelectionne.getCartesWagon().remove(wagon);                   //On supprime de sa main la carte wagon en question
            joueurSelectionne.getJeu().defausserCarteWagon(wagon);
        }
        joueurSelectionne.getCartesWagonPosees().clear();
        setProprietaire(joueurSelectionne);
        return getLongueur();
    }
}
