package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "livres")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Livre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titre;
    @Column(nullable = false, unique = true, length = 13)
    private String isbn;
    private String genre;
    private int anneePublication;
    @Column(precision = 10, scale = 2)
    private BigDecimal prix;
    private boolean disponible;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Auteur auteur;

    public Livre() { }
    public Livre(String titre, String isbn, String genre, int anneePublication,
                 BigDecimal prix, boolean disponible) {
        this.titre = titre;
        this.isbn = isbn;
        this.genre = genre;
        this.anneePublication = anneePublication;
        this.prix = prix;
        this.disponible = disponible;
    }
    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getAnneePublication() { return anneePublication; }
    public void setAnneePublication(int anneePublication) { this.anneePublication = anneePublication; }
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public Auteur getAuteur() { return auteur; }
    // Les modifications de relation passent par Auteur.ajouterLivre/retirerLivre.
    void setAuteur(Auteur auteur) { this.auteur = auteur; }
}
