package com.example.service;

import com.example.model.Auteur;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.function.Consumer;

public class PerformanceTestService {
    private final EntityManagerFactory emf;
    private final SessionFactory sessionFactory;
    private final Statistics statistiques;

    public PerformanceTestService(EntityManagerFactory emf) {
        this.emf = emf;
        sessionFactory = emf.unwrap(SessionFactory.class);
        statistiques = sessionFactory.getStatistics();
    }

    public void testPerformanceSansCache() {
        mesurer("Sans cache : N+1", false, em -> parcourir(em.createQuery(
                "SELECT a FROM Auteur a ORDER BY a.id", Auteur.class).getResultList()), 11);
    }

    public void testPerformanceAvecJoinFetch() {
        mesurer("JOIN FETCH sans cache", false, em -> parcourir(em.createQuery(
                "SELECT DISTINCT a FROM Auteur a LEFT JOIN FETCH a.livres", Auteur.class)
                .getResultList()), 1);
    }

    public void testPerformanceAvecEntityGraph() {
        mesurer("Entity Graph sans cache", false, em -> {
            TypedQuery<Auteur> query = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class);
            query.setHint("javax.persistence.fetchgraph", em.getEntityGraph("Auteur.livres"));
            parcourir(query.getResultList());
        }, 1);
    }

    public void testPerformanceAvecCache(Long auteurId) {
        sessionFactory.getCache().evictAllRegions();
        Consumer<EntityManager> lecture = em -> {
            Auteur auteur = em.find(Auteur.class, auteurId);
            if (auteur == null || auteur.getLivres().size() != 5) {
                throw new IllegalStateException("Auteur ou livres manquants");
            }
            // Initialisation des entites Livre, pas seulement de la collection.
            auteur.getLivres().forEach(livre -> livre.getTitre());
        };
        mesurer("Cache L2 : premiere session", true, lecture, 2);
        mesurer("Cache L2 : deuxieme session", true, lecture, 0);
        if (statistiques.getSecondLevelCacheHitCount() == 0) {
            throw new IllegalStateException("Aucun hit du cache L2");
        }
    }

    public void testCacheRequetes() {
        sessionFactory.getCache().evictAllRegions();
        Consumer<EntityManager> lecture = em -> {
            List<Long> ids = em.createQuery("SELECT a.id FROM Auteur a ORDER BY a.id", Long.class)
                    .setHint("org.hibernate.cacheable", true).getResultList();
            if (ids.size() != 10) throw new IllegalStateException("10 identifiants attendus");
        };
        mesurer("Cache requetes : premiere session", true, lecture, 1);
        mesurer("Cache requetes : deuxieme session", true, lecture, 0);
        if (statistiques.getQueryCacheHitCount() == 0) {
            throw new IllegalStateException("Aucun hit du cache de requetes");
        }
    }

    private void parcourir(List<Auteur> auteurs) {
        int livres = auteurs.stream().mapToInt(a -> a.getLivres().size()).sum();
        if (auteurs.size() != 10 || livres != 50) {
            throw new IllegalStateException("10 auteurs et 50 livres attendus");
        }
        System.out.println("Auteurs : " + auteurs.size() + " | Livres : " + livres);
    }

    private void mesurer(String titre, boolean cache, Consumer<EntityManager> action, long sqlAttendu) {
        System.out.println("\n--- " + titre + " ---");
        statistiques.clear();
        EntityManager em = emf.createEntityManager();
        long debut = System.nanoTime();
        try {
            em.unwrap(Session.class).setCacheMode(cache ? CacheMode.NORMAL : CacheMode.IGNORE);
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
        long sql = statistiques.getPrepareStatementCount();
        System.out.printf(java.util.Locale.ROOT,
                "Duree : %.3f ms | SQL : %d | L2 hits : %d | L2 misses : %d | L2 puts : %d | Query hits : %d%n",
                (System.nanoTime() - debut) / 1_000_000.0, sql,
                statistiques.getSecondLevelCacheHitCount(), statistiques.getSecondLevelCacheMissCount(),
                statistiques.getSecondLevelCachePutCount(), statistiques.getQueryCacheHitCount());
        if (sql != sqlAttendu) {
            throw new IllegalStateException(titre + " : " + sqlAttendu + " requetes attendues, " + sql + " obtenues");
        }
        System.out.println("Verification OK");
    }
}
