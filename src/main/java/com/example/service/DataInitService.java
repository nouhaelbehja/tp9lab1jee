package com.example.service;

import com.example.model.Auteur;
import com.example.model.Livre;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DataInitService {
    private final EntityManagerFactory emf;
    public DataInitService(EntityManagerFactory emf) { this.emf = emf; }

    public Long initData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Long premierId = null;
            String[] nationalites = {"Marocaine", "Francaise", "Senegalaise", "Canadienne", "Tunisienne"};
            String[] genres = {"Roman", "Histoire", "Informatique", "Poesie", "Science"};
            for (int i = 1; i <= 10; i++) {
                Auteur auteur = new Auteur("Auteur " + i, nationalites[(i - 1) % 5],
                        LocalDate.of(1960 + i, i, 10), "auteur" + i + "@example.com");
                for (int j = 1; j <= 5; j++) {
                    // Prefixe 978 + neuf chiffres + cle de controle ISBN-13.
                    String base = "978" + String.format(java.util.Locale.ROOT, "%09d", i * 100 + j);
                    int somme = 0;
                    for (int k = 0; k < 12; k++) somme += (base.charAt(k) - '0') * (k % 2 == 0 ? 1 : 3);
                    String isbn = base + ((10 - somme % 10) % 10);
                    auteur.ajouterLivre(new Livre("Livre " + j + " de l'auteur " + i,
                            isbn, genres[j - 1], 2015 + j, BigDecimal.valueOf(80 + i * 5 + j), j % 2 != 0));
                }
                em.persist(auteur);
                if (premierId == null) premierId = auteur.getId();
            }
            em.getTransaction().commit();
            System.out.println("Donnees initialisees : 10 auteurs et 50 livres (prix en MAD).");
            return premierId;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
