package fr.umontpellier.iut.gui;

import fr.umontpellier.iut.rails.Jeu;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class GameServer {

    private static ArrayList<Session> clients = new ArrayList<>();  //Liste des clients connectés au serveur
    private static String etatJeu = "";                             //Description de l'état du jeu, envoyé aux clients pour la mise à jour de l'interface graphique
    private static Jeu jeu;                                         //Instance de jeu exécutée par le serveur

    public static void main(String[] args) {

        jeu = new Jeu(new String[]{"Guybrush", "Largo", "LeChuck", "Elaine"});  // Lancement de la partie
        Server server = new Server("localhost", 3232, "/", WebSocketClient.class); // Prépare le serveur websocket

        try (Scanner scanner = new Scanner(System.in)) {
            server.start();             // lance le serveur
            new Thread(jeu).start();    // démarre le jeu (exécute la méthode Jeu.run() dans un nouveau thread)

            while (true) {
                jeu.addInput(scanner.nextLine());
            }

        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }

    /**
     * Ajoute une nouvelle instruction à la file d'instructions
     * (cette méthode est appelée lorsqu'un message est reçue sur la websocket)
     * @param message l'instruction à ajouter
     */
    public static void addInput(String message) {
        jeu.addInput(message);
    }

    /**
     * Met à jour l'état de la partie et envoie le nouvel état à tous les clients connectés
     * @param etatJeu l'état de la partie
     */
    public static void setEtatJeu(String etatJeu) {
        GameServer.etatJeu = etatJeu;
        // Envoie l'état de la partie à tous les clients
        try {
            for (Session session : clients) {
                session.getBasicRemote().sendText(etatJeu);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajoute un nouveau client à la liste, et lui transmet l'état actuel de la partie
     * (cette méthode est appelée lorsqu'une nouvelle connexion est établie)
     * @param session la session du nouveau client
     */
    public static void addClient(Session session) {
        GameServer.clients.add(session);
        try {
            session.getBasicRemote().sendText(etatJeu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retire un client de la liste
     * (cette méthode est appelée lorsqu'une connexion est fermée)
     * @param session la session du client à retirer
     */
    public static void removeClient(Session session) {
        GameServer.clients.remove(session);
    }
}
