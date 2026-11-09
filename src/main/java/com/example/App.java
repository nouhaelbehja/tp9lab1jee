package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        try {
            Long auteurId = new DataInitService(emf).initData();
            PerformanceTestService service = new PerformanceTestService(emf);
            service.testPerformanceSansCache();
            service.testPerformanceAvecJoinFetch();
            service.testPerformanceAvecEntityGraph();
            service.testPerformanceAvecCache(auteurId);
            service.testCacheRequetes();
            System.out.println("\nToutes les verifications du TP sont terminees.");
        } finally {
            emf.close();
        }
    }
}
